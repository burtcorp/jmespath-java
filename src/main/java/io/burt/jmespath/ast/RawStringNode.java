package io.burt.jmespath.ast;

public class RawStringNode extends JmesPathNode {
  private final String string;

  public RawStringNode(String string) {
    this.string = string;
  }

  protected String string() {
    return string;
  }

  @Override
  public String toString() {
    return String.format("RawStringNode(%s)", string);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RawStringNode)) {
      return false;
    }
    RawStringNode other = (RawStringNode) o;
    return this.string().equals(other.string());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + string.hashCode();
    return h;
  }
}
