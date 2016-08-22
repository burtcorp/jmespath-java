package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class ExpressionReferenceNode<T> extends Node<T> {
  private final Expression<T> expression;

  public ExpressionReferenceNode(Adapter<T> runtime, Expression<T> expression) {
    super(runtime, runtime.nodeFactory().createCurrent());
    this.expression = expression;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return this;
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
    return expression().search(currentValue);
  }

  protected Expression<T> expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("ExpressionReference(%s)", expression.toString());
  }

  @Override
  protected boolean internalEquals(Object o) {
    ExpressionReferenceNode<?> other = (ExpressionReferenceNode<?>) o;
    return expression().equals(other.expression());
  }

  @Override
  protected int internalHashCode() {
    return expression.hashCode();
  }
}
