package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public class OperatorNode<T> extends Node<T> {
  private final List<Node<T>> operands;

  @SafeVarargs
  public OperatorNode(Adapter<T> runtime, Node<T>... operands) {
    super(runtime);
    this.operands = Arrays.asList(operands);
  }

  protected List<Node<T>> operands() {
    return operands;
  }

  protected Node<T> operand(int index) {
    return operands.get(index);
  }

  @Override
  protected String internalToString() {
    StringBuilder operandsString = new StringBuilder();
    Iterator<Node<T>> operandIterator = operands.iterator();
    while (operandIterator.hasNext()) {
      Node<T> operand = operandIterator.next();
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
    for (Node<T> node : operands) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
