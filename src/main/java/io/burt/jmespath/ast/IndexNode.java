package io.burt.jmespath.ast;

public class IndexNode extends JmesPathNode {
  private final int index;

  public IndexNode(int index) {
    this.index = index;
  }

  protected int index() {
    return index;
  }

  @Override
  public String toString() {
    return String.format("IndexNode(%d)", index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IndexNode)) {
      return false;
    }
    IndexNode other = (IndexNode) o;
    return this.index() == other.index();
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + index;
    return h;
  }
}
