package io.burt.jmespath.function;

import io.burt.jmespath.Expression;

/**
 * In JMESPath most functions take regular values as arguments, but some are
 * higher order functions and take expressions. This class exists so that
 * argument lists can have a single type, while still contain a mix of values
 * and expressions.
 */
public abstract class FunctionArgument<T> {
  private static class V<U> extends FunctionArgument<U> {
    private final U value;

    public V(U value) {
      this.value = value;
    }

    @Override
    public U value() { return value; }

    @Override
    public boolean isValue() { return true; }
  }

  private static class E<U> extends FunctionArgument<U> {
    private final Expression<U> expression;

    public E(Expression<U> expression) {
      this.expression = expression;
    }

    @Override
    public Expression<U> expression() { return expression; }

    @Override
    public boolean isExpression() { return true; }
  }

  /**
   * Creates a new function argument that contains a value.
   */
  public static <U> FunctionArgument<U> of(U value) {
    return new V<>(value);
  }

  /**
   * Creates a new function argument that contains an expression.
   */
  public static <U> FunctionArgument<U> of(Expression<U> expression) {
    return new E<>(expression);
  }

  private FunctionArgument() { }

  /**
   * Returns the expression contained in this argument, or null if this argument
   * is not an expression argument.
   */
  public Expression<T> expression() { return null; }

  /**
   * Returns the value contained in this argument, or null if this argument is
   * not a value argument.
   */
  public T value() { return null; }

  /** Returns true when this argument contains an expression */
  public boolean isExpression() { return false; }

  /** Returns true when this argument contains a value */
  public boolean isValue() { return false; }
}
