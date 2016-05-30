package io.burt.jmespath.function;

import io.burt.jmespath.ast.JmesPathNode;

public class ExpressionOrValue<T> {
  private final JmesPathNode expression;
  private final T value;

  public ExpressionOrValue(JmesPathNode expression) {
    this.expression = expression;
    this.value = null;
  }

  public ExpressionOrValue(T value) {
    this.expression = null;
    this.value = value;
  }

  public JmesPathNode expression() {
    return expression;
  }

  public T value() {
    return value;
  }

  public boolean isExpression() {
    return expression != null;
  }

  public boolean isValue() {
    return value != null;
  }
}
