package io.burt.jmespath.function;

import io.burt.jmespath.Expression;

public abstract class FunctionArgument<T> {
  private static class V<U> extends FunctionArgument<U> {
    private final U value;

    public V(U value) {
      this.value = value;
    }

    public U value() { return value; }

    public boolean isValue() { return true; }
  }

  private static class E<U> extends FunctionArgument<U> {
    private final Expression<U> expression;

    public E(Expression<U> expression) {
      this.expression = expression;
    }

    public Expression<U> expression() { return expression; }

    public boolean isExpression() { return true; }
  }

  public static <U> FunctionArgument<U> of(U value) {
    return new V<U>(value);
  }

  public static <U> FunctionArgument<U> of(Expression<U> expression) {
    return new E<U>(expression);
  }

  private FunctionArgument() { }

  public Expression<T> expression() { return null; };

  public T value() { return null; }

  public boolean isExpression() { return false; }

  public boolean isValue() { return false; }
}
