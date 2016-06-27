package io.burt.jmespath;

import java.util.List;

import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

import static io.burt.jmespath.JmesPathType.*;

/**
 * This class can be extended instead of implementing {@link Adapter} directly,
 * in order to not have to implement a few of the methods that have non-specific
 * implementations, like {@link Adapter#callFunction}, {@link Adapter#typeOf}
 * or the {@link Comparable} interface. Subclasses are encouraged to override
 * these methods if they have more efficient means to perform the same job.
 */
public abstract class BaseAdapter<T> implements Adapter<T> {
  private final FunctionRegistry functionRegistry;

  /**
   * Create a new adapter with a default function registry.
   */
  public BaseAdapter() {
    this(FunctionRegistry.createDefaultRegistry());
  }

  /**
   * Create a new adapter with a custom function registry.
   */
  public BaseAdapter(FunctionRegistry functionRegistry) {
    this.functionRegistry = functionRegistry;
  }

  /**
   * Basic implementation of {@link Adapter#typeOf}.
   *
   * Subclasses should override this method if they have a more efficient way to
   * determine the type of a value than using the methods {@link Adapter#isNull},
   * {@link Adapter#isBoolean}, etc.
   */
  @Override
  public JmesPathType typeOf(T value) {
    if (isNull(value)) {
      return NULL;
    } else if (isBoolean(value)) {
      return BOOLEAN;
    } else if (isNumber(value)) {
      return NUMBER;
    } else if (isArray(value)) {
      return ARRAY;
    } else if (isObject(value)) {
      return OBJECT;
    } else if (isString(value)) {
      return STRING;
    } else {
      throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  /**
   * Basic implementation of {@link Adapter#compare}.
   *
   * Subclasses should override this method if they have a more efficient way to
   * compare booleans, numbers and strings than to convert them to Java types
   * using {@link Adapter#isTruthy}, {@link Adapter#toNumber}, {@link Adapter#toString}, etc.
   *
   * This only implements {@link Comparator#compare} fully for <code>null</code>,
   * <code>boolean</code>, <code>number</code> and <code>string</code>, for
   * <code>array</code> and <code>object</code> it only does equality –
   * specifically this means that it will return 0 for equal objects or arrays,
   * and -1 otherwise. The reason is that JMESPath doesn't have any mechanisms
   * for comparing objects or arrays, and doesn't define how objects and arrays
   * should be compared.
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
          return (isTruthy(value1) && isTruthy(value2)) || (!isTruthy(value1) && !isTruthy(value2)) ? 0 : -1;
        case NUMBER:
          Comparable d1 = toNumber(value1).doubleValue();
          Comparable d2 = toNumber(value2).doubleValue();
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
   * Very few adapters will have any reason to override this method, it only
   * proxies the call to {@link FunctionRegistry#callFunction} passing itself
   * along as the adapter.
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
    return o.getClass().isAssignableFrom(this.getClass());
  }
}
