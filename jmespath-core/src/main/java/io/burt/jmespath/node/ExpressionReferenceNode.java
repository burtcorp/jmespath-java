package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class ExpressionReferenceNode extends JmesPathNode {
  private final JmesPathNode expression;

  public ExpressionReferenceNode(JmesPathNode expression) {
    super(new CurrentNode());
    this.expression = expression;
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> runtime, T currentValue) {
    return expression().evaluate(runtime, currentValue);
  }

  protected JmesPathNode expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("ExpressionReference(%s)", expression.toString());
  }

  @Override
  protected boolean internalEquals(Object o) {
    ExpressionReferenceNode other = (ExpressionReferenceNode) o;
    return expression().equals(other.expression());
  }

  @Override
  protected int internalHashCode() {
    return expression.hashCode();
  }
}
