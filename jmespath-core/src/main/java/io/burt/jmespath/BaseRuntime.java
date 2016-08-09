package io.burt.jmespath;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.parser.JmesPathExpressionParser;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

/**
 * This class can be extended instead of implementing {@link JmesPathRuntime}
 * directly, in order to not have to implement a few of the methods that have
 * non-specific implementations, like {@link JmesPathRuntime#callFunction},
 * {@link JmesPathRuntime#typeOf} or the {@link Comparable} interface. Subclasses are
 * encouraged to override these methods if they have more efficient means to
 * perform the same job.
 */
public abstract class BaseRuntime<T> implements Adapter<T> {
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
  public JmesPathExpression<T> compile(String expression) {
    return JmesPathExpressionParser.fromString(this, expression);
  }

  /**
   * Basic implementation of {@link JmesPathRuntime#compare}.
   *
   * Subclasses should override this method if they have a more efficient way to
   * compare booleans, numbers and strings than to convert them to Java types
   * using {@link JmesPathRuntime#isTruthy}, {@link JmesPathRuntime#toNumber},
   * {@link JmesPathRuntime#toString}, etc.
   *
   * This only implements {@link Comparator#compare} fully for <code>null</code>,
   * <code>number</code> and <code>string</code>, for <code>boolean</code>
   * <code>array</code> and <code>object</code> it only does equality –
   * specifically this means that it will return 0 for equal booleans, objects
   * or arrays, and -1 otherwise. The reason is that JMESPath doesn't have any
   * mechanisms for comparing objects or arrays, and doesn't define how objects
   * and arrays should be compared.
   *
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
   * Calls a function with the specified arguments and returns the result.
   *
   * Very few runtimes will have any reason to override this method, it only
   * proxies the call to {@link FunctionRegistry#callFunction} passing itself
   * along as the runtime.
   */
  @Override
  public T callFunction(String name, List<ExpressionOrValue<T>> arguments) {
    return functionRegistry.callFunction(this, name, arguments);
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
  protected String unparse(T obj) {
    switch (typeOf(obj)) {
      case NUMBER:
      case BOOLEAN:
      case NULL:
        return obj.toString();
      case STRING:
        return String.format("\"%s\"", obj);
      case OBJECT:
        return unparseObject(obj);
      case ARRAY:
        return unparseArray(obj);
      default:
        throw new IllegalStateException();
    }
  }

  private String unparseObject(T object) {
    StringBuilder str = new StringBuilder("{");
    Iterator<T> keys = getPropertyNames(object).iterator();
    while (keys.hasNext()) {
      T key = keys.next();
      str.append("\"").append(toString(key)).append("\"");
      str.append(":").append(unparse(getProperty(object, key)));
      if (keys.hasNext()) {
        str.append(",");
      }
    }
    str.append("}");
    return str.toString();
  }

  private String unparseArray(T array) {
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
