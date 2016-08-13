package io.burt.jmespath;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.parser.ExpressionParser;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.util.StringEscapeHelper;

/**
 * This class can be extended instead of implementing {@link Adapter} directly,
 * in order to not have to implement a few of the methods that have non-specific
 * implementations, like {@link Adapter#getFunction}, {@link Adapter#typeOf} or
 * the {@link Comparable} interface. Subclasses are encouraged to override these
 * methods if they have more efficient means to perform the same job.
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

  private final FunctionRegistry functionRegistry;

  /**
   * Create a new runtime with a default function registry.
   */
  public BaseRuntime() {
    this(null);
  }

  /**
   * Create a new runtime with a custom function registry.
   */
  public BaseRuntime(FunctionRegistry functionRegistry) {
    if (functionRegistry == null) {
      this.functionRegistry = FunctionRegistry.defaultRegistry();
    } else {
      this.functionRegistry = functionRegistry;
    }
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
   * This only implements {@link Comparator#compare} fully for <code>null</code>,
   * <code>number</code> and <code>string</code>, for <code>boolean</code>
   * <code>array</code> and <code>object</code> it only does equality –
   * specifically this means that it will return 0 for equal booleans, objects
   * or arrays, and -1 otherwise. The reason is that JMESPath doesn't have any
   * mechanisms for comparing objects or arrays, and doesn't define how objects
   * and arrays should be compared.
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
          Double d1 = toNumber(value1).doubleValue();
          Double d2 = toNumber(value2).doubleValue();
          return d1.compareTo(d2);
        case STRING:
          String s1 = toString(value1);
          String s2 = toString(value2);
          return s1.compareTo(s2);
        case ARRAY:
        case OBJECT:
          return value1.equals(value2) ? 0 : -1;
        default:
          throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getClass().getName()));
      }
    } else {
      return -1;
    }
  }

  /**
   * Returns the function by the specified name or null if no such function exists.
   * <p>
   * Very few runtimes will have any reason to override this method, it only
   * proxies the call to {@link FunctionRegistry#getFunction}.
   */
  @Override
  public Function getFunction(String name) {
    return functionRegistry.getFunction(name);
  }

  /**
   * Required method from the {@link Comparator} interface, returns true when
   * the argument is of the same class, or a subclass of, the receiver.
   */
  @Override
  public boolean equals(Object o) {
    return getClass().isInstance(o);
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
    return object.toString();
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
      str.append("\"").append(escapeString(toString(key))).append("\"");
      str.append(":").append(unparse(getProperty(object, key)));
      if (keys.hasNext()) {
        str.append(",");
      }
    }
    str.append("}");
    return str.toString();
  }

  protected String unparseArray(T array) {
    StringBuilder str = new StringBuilder("[");
    Iterator<T> elements = toList(array).iterator();
    while (elements.hasNext()) {
      str.append(unparse(elements.next()));
      if (elements.hasNext()) {
        str.append(",");
      }
    }
    str.append("]");
    return str.toString();
  }
}
