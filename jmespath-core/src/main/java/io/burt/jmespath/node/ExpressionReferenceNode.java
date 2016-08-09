package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class ExpressionReferenceNode<T> extends JmesPathNode<T> {
  private final JmesPathNode<T> expression;

  public ExpressionReferenceNode(Adapter<T> runtime, JmesPathNode<T> expression) {
    super(runtime, new CurrentNode<T>(runtime));
    this.expression = expression;
  }

  @Override
  protected T searchOne(T currentValue) {
    return expression().search(currentValue);
  }

  protected JmesPathNode<T> expression() {
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
