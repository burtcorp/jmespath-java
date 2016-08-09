package io.burt.jmespath.node;

import java.util.Arrays;

import io.burt.jmespath.Adapter;

public class OperatorNode<T> extends JmesPathNode<T> {
  private final JmesPathNode<T>[] operands;

  @SafeVarargs
  public OperatorNode(Adapter<T> runtime, JmesPathNode<T>... operands) {
    super(runtime);
    this.operands = operands;
  }

  protected JmesPathNode<T>[] operands() {
    return operands;
  }

  @Override
  protected String internalToString() {
    StringBuilder operandsString = new StringBuilder();
    for (JmesPathNode<T> node : operands) {
      operandsString.append(", ").append(node);
    }
    return operandsString.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    OperatorNode<T> other = (OperatorNode<T>) o;
    return Arrays.equals(operands(), other.operands());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (JmesPathNode<T> node : operands) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
