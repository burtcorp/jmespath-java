package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class ExpressionReferenceNode extends JmesPathNode {
  private final JmesPathNode expression;

  public ExpressionReferenceNode(JmesPathNode expression) {
    super(null);
    this.expression = expression;
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    String name = getClass().getName();
    name = name.substring(name.lastIndexOf(".") + 1);
    throw new UnsupportedOperationException(String.format("%s#evaluate not implemented", name));
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
