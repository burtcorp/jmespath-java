package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class ComparisonNode<T> extends OperatorNode<T> {
  private final String operator;

  public ComparisonNode(Adapter<T> runtime, String operator, Expression<T> left, Expression<T> right) {
    super(runtime, left, right);
    this.operator = operator;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return this;
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
    T leftResult = operand(0).search(currentValue);
    T rightResult = operand(1).search(currentValue);
    JmesPathType leftType = runtime.typeOf(leftResult);
    JmesPathType rightType = runtime.typeOf(rightResult);
    if (leftType == JmesPathType.NUMBER && rightType == JmesPathType.NUMBER) {
      return compareNumbers(leftResult, rightResult);
    } else {
      return compareObjects(leftResult, rightResult);
    }
  }

  private T compareObjects(T leftResult, T rightResult) {
    int result = runtime.compare(leftResult, rightResult);
    if (operator.equals("==")) {
      return runtime.createBoolean(result == 0);
    } else if (operator.equals("!=")) {
      return runtime.createBoolean(result != 0);
    }
    return runtime.createNull();
  }

  private T compareNumbers(T leftResult, T rightResult) {
    int result = runtime.compare(leftResult, rightResult);
    if (operator.equals("==")) {
      return runtime.createBoolean(result == 0);
    } else if (operator.equals("!=")) {
      return runtime.createBoolean(result != 0);
    } else if (operator.equals(">")) {
      return runtime.createBoolean(result > 0);
    } else if (operator.equals(">=")) {
      return runtime.createBoolean(result >= 0);
    } else if (operator.equals("<")) {
      return runtime.createBoolean(result < 0);
    } else if (operator.equals("<=")) {
      return runtime.createBoolean(result <= 0);
    }
    return runtime.createNull();
  }

  protected String operator() {
    return operator;
  }

  @Override
  protected String internalToString() {
    return String.format("%s, %s, %s", operator, operand(0), operand(1));
  }

  @Override
  protected boolean internalEquals(Object o) {
    if (super.internalEquals(o)) {
      ComparisonNode<?> other = (ComparisonNode<?>) o;
      return operator().equals(other.operator());
    } else {
      return false;
    }
  }

  @Override
  protected int internalHashCode() {
    return operator.hashCode();
  }
}
