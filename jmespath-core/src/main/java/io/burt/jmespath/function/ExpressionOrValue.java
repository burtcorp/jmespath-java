package io.burt.jmespath.function;

import io.burt.jmespath.node.Node;

public class ExpressionOrValue<T> {
  private final Node<T> expression;
  private final T value;

  public ExpressionOrValue(Node<T> expression) {
    this.expression = expression;
    this.value = null;
  }

  public ExpressionOrValue(T value) {
    this.expression = null;
    this.value = value;
  }

  public Node<T> expression() {
    return expression;
  }

  public T value() {
    return value;
  }

  public boolean isExpression() {
    return expression != null;
  }

  public boolean isValue() {
    return expression == null;
  }
}
