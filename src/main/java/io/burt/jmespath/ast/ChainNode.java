package io.burt.jmespath.ast;

import java.util.Arrays;

public class ChainNode extends JmesPathNode {
  private final JmesPathNode[] chain;

  public ChainNode(JmesPathNode... chain) {
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
    return String.format("ChainNode(%s)", chainString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChainNode)) {
      return false;
    }
    ChainNode other = (ChainNode) o;
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
