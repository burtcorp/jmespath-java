package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ComparisonNode<T> extends OperatorNode<T> {
  private final String operator;

  public ComparisonNode(Adapter<T> runtime, String operator, JmesPathNode<T> left, JmesPathNode<T> right) {
    super(runtime, left, right);
    this.operator = operator;
  }

  @Override
  protected T evaluateOne(T currentValue) {
    T leftResult = operands()[0].evaluate(currentValue);
    T rightResult = operands()[1].evaluate(currentValue);
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
    return String.format("%s, %s, %s", operator, operands()[0], operands()[1]);
  }

  @Override
  protected boolean internalEquals(Object o) {
    ComparisonNode other = (ComparisonNode) o;
    return operator().equals(other.operator());
  }

  @Override
  protected int internalHashCode() {
    return operator.hashCode();
  }
}
