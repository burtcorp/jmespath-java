package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public class OperatorNode<T> extends JmesPathNode<T> {
  private final List<JmesPathNode<T>> operands;

  @SafeVarargs
  public OperatorNode(Adapter<T> runtime, JmesPathNode<T>... operands) {
    super(runtime);
    this.operands = Arrays.asList(operands);
  }

  protected List<JmesPathNode<T>> operands() {
    return operands;
  }

  protected JmesPathNode<T> operand(int index) {
    return operands.get(index);
  }

  @Override
  protected String internalToString() {
    StringBuilder operandsString = new StringBuilder();
    Iterator<JmesPathNode<T>> operandIterator = operands.iterator();
    while (operandIterator.hasNext()) {
      JmesPathNode<T> operand = operandIterator.next();
      operandsString.append(operand);
      if (operandIterator.hasNext()) {
        operandsString.append(", ");
      }
    }
    return operandsString.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    OperatorNode<T> other = (OperatorNode<T>) o;
    return operands().equals(other.operands());
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
