package io.burt.jmespath.ast;

import java.util.Arrays;

public class SequenceNode extends JmesPathNode {
  private final JmesPathNode[] sequence;

  public SequenceNode(JmesPathNode... sequence) {
    this.sequence = sequence;
  }

  protected JmesPathNode[] sequence() {
    return sequence;
  }

  @Override
  public String toString() {
    StringBuilder sequenceString = new StringBuilder();
    for (JmesPathNode node : sequence) {
      sequenceString.append(", ").append(node);
    }
    sequenceString.delete(0, 2);
    return String.format("%s(%s)", getClass().getName(), sequenceString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!o.getClass().isAssignableFrom(this.getClass())) {
      return false;
    }
    SequenceNode other = (SequenceNode) o;
    return Arrays.equals(this.sequence(), other.sequence());
  }

  @Override
  public int hashCode() {
    int h = 1;
    for (JmesPathNode node : sequence) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
