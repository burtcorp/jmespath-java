package io.burt.jmespath.ast;

public class StringNode extends JmesPathNode {
  private final String string;

  public StringNode(String string) {
    this.string = string;
  }

  protected String string() {
    return string;
  }

  @Override
  public String toString() {
    return String.format("StringNode(%s)", string);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StringNode)) {
      return false;
    }
    StringNode other = (StringNode) o;
    return this.string().equals(other.string());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + string.hashCode();
    return h;
  }
}
