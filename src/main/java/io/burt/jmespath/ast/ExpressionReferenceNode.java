package io.burt.jmespath.ast;

public class ExpressionReferenceNode extends JmesPathNode {
  private final JmesPathNode expression;

  public ExpressionReferenceNode(JmesPathNode expression) {
    super(null);
    this.expression = expression;
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
