package io.burt.jmespath.ast;

import java.util.Arrays;

public class FunctionCallNode extends JmesPathNode {
  private final String name;
  private final JmesPathNode[] args;

  public FunctionCallNode(String name, JmesPathNode... args) {
    this.name = name;
    this.args = args;
  }

  protected String name() {
    return name;
  }

  protected JmesPathNode[] args() {
    return args;
  }

  @Override
  public String toString() {
    StringBuilder argsString = new StringBuilder();
    for (JmesPathNode node : args) {
      argsString.append(", ").append(node);
    }
    argsString.delete(0, 2);
    return String.format("FunctionCallNode(%s, [%s])", name, argsString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FunctionCallNode)) {
      return false;
    }
    FunctionCallNode other = (FunctionCallNode) o;
    return this.name().equals(other.name()) && Arrays.equals(this.args(), other.args());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + name.hashCode();
    for (JmesPathNode node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
