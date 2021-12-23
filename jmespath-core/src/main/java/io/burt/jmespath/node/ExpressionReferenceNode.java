package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class ExpressionReferenceNode<T> extends Node<T> {
  private final Expression<T> expression;

  public ExpressionReferenceNode(Adapter<T> runtime, Expression<T> expression) {
    super(runtime);
    this.expression = expression;
  }

  public Expression<T> expression() {
    return expression;
  }

  @Override
  public T search(T input) {
    return expression.search(input);
  }

  @Override
  public String toString() {
    return String.format("ExpressionReference(%s)", expression.toString());
  }

  @Override
  protected boolean internalEquals(Object o) {
    ExpressionReferenceNode<?> other = (ExpressionReferenceNode<?>) o;
    return expression.equals(other.expression);
  }

  @Override
  protected int internalHashCode() {
    return expression.hashCode();
  }
}
