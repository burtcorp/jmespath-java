package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public abstract class OperatorNode<T> extends Node<T> {
  private final List<Expression<T>> operands;

  @SafeVarargs
  public OperatorNode(Adapter<T> runtime, Expression<T>... operands) {
    super(runtime);
    this.operands = Arrays.asList(operands);
  }

  protected Expression<T> operand(int index) {
    return operands.get(index);
  }

  @Override
  protected String internalToString() {
    if (operands.isEmpty()) {
      return "";
    }

    StringBuilder operandsString = new StringBuilder();
    for (Expression<T> operand : operands) {
      operandsString.append(operand).append(", ");
    }
    operandsString.setLength(operandsString.length() - 2);
    return operandsString.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    OperatorNode<?> other = (OperatorNode<?>) o;
    return operands.equals(other.operands);
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
