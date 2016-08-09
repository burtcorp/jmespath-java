package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class ComparisonNode extends OperatorNode {
  private final String operator;

  public ComparisonNode(String operator, JmesPathNode left, JmesPathNode right) {
    super(left, right);
    this.operator = operator;
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    T leftResult = operands()[0].evaluate(runtime, currentValue);
    T rightResult = operands()[1].evaluate(runtime, currentValue);
    JmesPathType leftType = runtime.typeOf(leftResult);
    JmesPathType rightType = runtime.typeOf(rightResult);
    if (leftType == JmesPathType.NUMBER && rightType == JmesPathType.NUMBER) {
      return compareNumbers(runtime, leftResult, rightResult);
    } else {
      return compareObjects(runtime, leftResult, rightResult);
    }
  }

  private <T> T compareObjects(JmesPathRuntime<T> runtime, T leftResult, T rightResult) {
    int result = runtime.compare(leftResult, rightResult);
    if (operator.equals("==")) {
      return runtime.createBoolean(result == 0);
    } else if (operator.equals("!=")) {
      return runtime.createBoolean(result != 0);
    }
    return runtime.createNull();
  }

  private <T> T compareNumbers(JmesPathRuntime<T> runtime, T leftResult, T rightResult) {
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
