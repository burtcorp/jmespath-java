package io.burt.jmespath.ast;

public class StringNode extends JmesPathNode {
  private final String string;

  public StringNode(String string) {
    super();
    this.string = string;
  }

  protected String string() {
    return string;
  }

  @Override
  public String toString() {
    return String.format("String(%s)", string);
  }

  @Override
  protected boolean internalEquals(Object o) {
    StringNode other = (StringNode) o;
    return string().equals(other.string());
  }

  @Override
  protected int internalHashCode() {
    return string.hashCode();
  }
}
