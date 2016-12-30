package io.burt.jmespath;

import java.util.List;
import java.util.Iterator;
import java.util.Collection;

import io.burt.jmespath.parser.ExpressionParser;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.function.ArityException;
import io.burt.jmespath.function.ArgumentTypeException;
import io.burt.jmespath.node.NodeFactory;
import io.burt.jmespath.node.StandardNodeFactory;
import io.burt.jmespath.util.StringEscapeHelper;

/**
 * This class can be extended instead of implementing {@link Adapter} directly,
 * in order to not have to implement a few of the methods that have non-specific
 * implementations, like {@link Adapter#functionRegistry}, {@link Adapter#typeOf}
 * or the {@link Comparable} interface. Subclasses are encouraged to override
 * these methods if they have more efficient means to perform the same job.
 */
public abstract class BaseRuntime<T> implements Adapter<T> {
  private static final StringEscapeHelper jsonEscapeHelper = new StringEscapeHelper(
    true,
    'b', '\b',
    't', '\t',
    'n', '\n',
    'f', '\f',
    'r', '\r',
    '\\', '\\',
    '\"', '\"'
  );

  private final RuntimeConfiguration configuration;
  private final FunctionRegistry functionRegistry;
  private final NodeFactory<T> nodeFactory;

  /**
   * Create a new runtime with a default function registry.
   */
  public BaseRuntime() {
    this(RuntimeConfiguration.defaultConfiguration());
  }

  /**
   * Create a new runtime with configuration.
   */
  public BaseRuntime(RuntimeConfiguration configuration) {
    this.configuration = configuration;
    this.functionRegistry = configuration.functionRegistry();
    this.nodeFactory = new StandardNodeFactory<>(this);
  }

  @Override
  public Expression<T> compile(String expression) {
    return ExpressionParser.fromString(this, expression);
  }

  /**
   * Basic implementation of {@link Adapter#compare}.
   * <p>
   * Subclasses should override this method if they have a more efficient way to
   * compare booleans, numbers and strings than to convert them to Java types
   * using {@link Adapter#isTruthy}, {@link Adapter#toNumber},
   * {@link Adapter#toString}, etc.
   * <p>
   * This only implements {@link java.util.Comparator#compare} fully for
   * <code>null</code>, <code>number</code> and <code>string</code>, for
   * <code>boolean</code> <code>array</code> and <code>object</code> it only
   * does equality – specifically this means that it will return 0 for equal
   * booleans, objects or arrays, and -1 otherwise. The reason is that JMESPath
   * doesn't have any mechanisms for comparing objects or arrays, and doesn't
   * define how objects and arrays should be compared.
   * <p>
   * When the arguments are not of the same type -1 is returned.
   */
  @Override
  public int compare(T value1, T value2) {
    JmesPathType type1 = typeOf(value1);
    JmesPathType type2 = typeOf(value2);
    if (type1 == type2) {
      switch (type1) {
        case NULL:
          return 0;
        case BOOLEAN:
          return isTruthy(value1) == isTruthy(value2) ? 0 : -1;
        case NUMBER:
          double d1 = toNumber(value1).doubleValue();
          double d2 = toNumber(value2).doubleValue();
          return Double.compare(d1, d2);
        case STRING:
          String s1 = toString(value1);
          String s2 = toString(value2);
          return s1.compareTo(s2);
        case ARRAY:
          return deepEqualsArray(value1, value2) ? 0 : -1;
        case OBJECT:
          return deepEqualsObject(value1, value2) ? 0 : -1;
        default:
          throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getClass().getName()));
      }
    } else {
      return -1;
    }
  }

  private boolean deepEqualsArray(T value1, T value2) {
    List<T> values1 = toList(value1);
    List<T> values2 = toList(value2);
    int size = values1.size();
    if (size != values2.size()) {
      return false;
    }
    for (int i = 0; i < size; i++) {
      if (compare(values1.get(i), values2.get(i)) != 0) {
        return false;
      }
    }
    return true;
  }

  private boolean deepEqualsObject(T value1, T value2) {
    Collection<T> keys1 = getPropertyNames(value1);
    Collection<T> keys2 = getPropertyNames(value2);
    if (keys1.size() != keys2.size()) {
      return false;
    }
    if (!keys1.containsAll(keys2)) {
      return false;
    }
    for (T key : keys1) {
      if (compare(getProperty(value1, key), getProperty(value2, key)) != 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Throws {@link ArgumentTypeException} unless {@link RuntimeConfiguration#silentTypeErrors}
   * is true, in which case it returns a null value (<em>not</em> Java <code>null</code>).
   */
  @Override
  public T handleArgumentTypeError(Function function, String expectedType, String actualType) {
    if (configuration.silentTypeErrors()) {
      return createNull();
    } else {
      throw new ArgumentTypeException(function, expectedType, actualType);
    }
  }

  @Override
  public FunctionRegistry functionRegistry() {
    return functionRegistry;
  }

  @Override
  public NodeFactory<T> nodeFactory() {
    return nodeFactory;
  }

  /**
   * Required method from the {@link java.util.Comparator} interface, returns
   * true when the argument is of the same class, or a subclass of, the receiver.
   */
  @Override
  public boolean equals(Object o) {
    return getClass().isInstance(o);
  }

  @Override
  public int hashCode() {
    return 31;
  }

  /**
   * Helper method to render a value as JSON.
   *
   * Assumes that <code>null</code>, <code>number</code> and <code>boolean</code>
   * render themseves correctly with <code>toString</code>, and that
   * <code>string</code> renders itself as an unquoted string.
   */
  protected String unparse(T object) {
    switch (typeOf(object)) {
      case NUMBER:
        return unparseNumber(object);
      case BOOLEAN:
        return unparseBoolean(object);
      case NULL:
        return unparseNull(object);
      case STRING:
        return unparseString(object);
      case OBJECT:
        return unparseObject(object);
      case ARRAY:
        return unparseArray(object);
      default:
        throw new IllegalStateException();
    }
  }

  protected String unparseNumber(T object) {
    return object.toString();
  }

  protected String unparseBoolean(T object) {
    return object.toString();
  }

  protected String unparseNull(T object) {
    return "null";
  }

  protected String unparseString(T object) {
    return String.format("\"%s\"", escapeString(toString(object)));
  }

  protected String escapeString(String str) {
    return jsonEscapeHelper.escape(str);
  }

  protected String unparseObject(T object) {
    StringBuilder str = new StringBuilder("{");
    Iterator<T> keys = getPropertyNames(object).iterator();
    while (keys.hasNext()) {
      T key = keys.next();
      T value = getProperty(object, key);
      str.append(unparseString(key));
      str.append(':');
      str.append(unparse(value));
      if (keys.hasNext()) {
        str.append(',');
      }
    }
    str.append('}');
    return str.toString();
  }

  protected String unparseArray(T array) {
    StringBuilder str = new StringBuilder("[");
    Iterator<T> elements = toList(array).iterator();
    while (elements.hasNext()) {
      str.append(unparse(elements.next()));
      if (elements.hasNext()) {
        str.append(',');
      }
    }
    str.append(']');
    return str.toString();
  }

  @Override
  @Deprecated
  public T getProperty(T value, String name) {
    return getProperty(value, createString(name));
  }
}
