package io.burt.jmespath.ast;

import java.util.Arrays;

public class FunctionCallNode extends JmesPathNode {
  private final String name;
  private final JmesPathNode[] args;

  public FunctionCallNode(String name, JmesPathNode[] args, JmesPathNode source) {
    super(source);
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
  protected String internalToString() {
    StringBuilder str = new StringBuilder(name).append(", [");
    for (JmesPathNode node : args) {
      str.append(node).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    FunctionCallNode other = (FunctionCallNode) o;
    return name().equals(other.name()) && Arrays.equals(args(), other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + name.hashCode();
    for (JmesPathNode node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
