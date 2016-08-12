package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class OperatorNode<T> extends Node<T> {
  private final List<Expression<T>> operands;

  @SafeVarargs
  public OperatorNode(Adapter<T> runtime, Expression<T>... operands) {
    super(runtime);
    this.operands = Arrays.asList(operands);
  }

  protected List<Expression<T>> operands() {
    return operands;
  }

  protected Expression<T> operand(int index) {
    return operands.get(index);
  }

  @Override
  protected String internalToString() {
    StringBuilder operandsString = new StringBuilder();
    Iterator<Expression<T>> operandIterator = operands.iterator();
    while (operandIterator.hasNext()) {
      Expression<T> operand = operandIterator.next();
      operandsString.append(operand);
      if (operandIterator.hasNext()) {
        operandsString.append(", ");
      }
    }
    return operandsString.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    OperatorNode other = (OperatorNode) o;
    return operands().equals(other.operands());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (Expression<T> node : operands) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
