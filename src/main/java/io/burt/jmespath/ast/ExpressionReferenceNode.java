package io.burt.jmespath.ast;

public class ExpressionReferenceNode extends JmesPathNode {
  private final JmesPathNode expression;

  public ExpressionReferenceNode(JmesPathNode expression) {
    this.expression = expression;
  }

  protected JmesPathNode expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("ExpressionReferenceNode(%s)", expression);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExpressionReferenceNode)) {
      return false;
    }
    ExpressionReferenceNode other = (ExpressionReferenceNode) o;
    return this.expression().equals(other.expression());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + expression.hashCode();
    return h;
  }
}
