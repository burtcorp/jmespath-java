package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class ExpressionReferenceNode<T> extends Node<T> {
  private final Node<T> expression;

  public ExpressionReferenceNode(Adapter<T> runtime, Node<T> expression) {
    super(runtime, new CurrentNode<T>(runtime));
    this.expression = expression;
  }

  @Override
  protected T searchOne(T currentValue) {
    return expression().search(currentValue);
  }

  protected Node<T> expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("ExpressionReference(%s)", expression.toString());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    ExpressionReferenceNode<T> other = (ExpressionReferenceNode<T>) o;
    return expression().equals(other.expression());
  }

  @Override
  protected int internalHashCode() {
    return expression.hashCode();
  }
}
