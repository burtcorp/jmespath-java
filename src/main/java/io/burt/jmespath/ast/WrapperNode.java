package io.burt.jmespath.ast;

public class WrapperNode extends JmesPathNode {
  private final JmesPathNode expression;

  public WrapperNode(JmesPathNode expression) {
    this.expression = expression;
  }

  protected JmesPathNode expression() {
    return expression;
  }

  @Override
  public String toString() {
    String name = getClass().getName();
    name = name.substring(name.lastIndexOf(".") + 1);
    return String.format("%s(%s)", name, expression);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!o.getClass().isAssignableFrom(this.getClass())) {
      return false;
    }
    WrapperNode other = (WrapperNode) o;
    return this.expression().equals(other.expression());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + expression.hashCode();
    return h;
  }
}
