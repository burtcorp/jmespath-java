package io.burt.jmespath.ast;

import java.util.Arrays;

public class SelectionNode extends JmesPathNode {
  private final JmesPathNode test;

  public SelectionNode(JmesPathNode test) {
    this.test = test;
  }

  protected JmesPathNode test() {
    return test;
  }

  @Override
  public String toString() {
    return String.format("SelectionNode(%s)", test);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SelectionNode)) {
      return false;
    }
    SelectionNode other = (SelectionNode) o;
    return this.test().equals(other.test());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + test.hashCode();
    return h;
  }
}
