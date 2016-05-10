package io.burt.jmespath.ast;

import java.util.Arrays;

public class PipeNode extends JmesPathNode {
  private final JmesPathNode[] chain;

  public PipeNode(JmesPathNode... chain) {
    this.chain = chain;
  }

  protected JmesPathNode[] chain() {
    return chain;
  }

  @Override
  public String toString() {
    StringBuilder chainString = new StringBuilder();
    for (JmesPathNode node : chain) {
      chainString.append(", ").append(node);
    }
    chainString.delete(0, 2);
    return String.format("PipeNode(%s)", chainString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PipeNode)) {
      return false;
    }
    PipeNode other = (PipeNode) o;
    return Arrays.equals(this.chain(), other.chain());
  }

  @Override
  public int hashCode() {
    int h = 1;
    for (JmesPathNode node : chain) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
