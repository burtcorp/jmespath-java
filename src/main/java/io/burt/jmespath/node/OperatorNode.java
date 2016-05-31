package io.burt.jmespath.node;

import java.util.Arrays;

public class OperatorNode extends JmesPathNode {
  private final JmesPathNode[] operands;

  public OperatorNode(JmesPathNode... operands) {
    super();
    this.operands = operands;
  }

  protected JmesPathNode[] operands() {
    return operands;
  }

  @Override
  protected String internalToString() {
    StringBuilder operandsString = new StringBuilder();
    for (JmesPathNode node : operands) {
      operandsString.append(", ").append(node);
    }
    return operandsString.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    OperatorNode other = (OperatorNode) o;
    return Arrays.equals(operands(), other.operands());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (JmesPathNode node : operands) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
