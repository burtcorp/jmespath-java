package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ComparisonNode extends OperatorNode {
  private final String operator;

  public ComparisonNode(String operator, JmesPathNode left, JmesPathNode right) {
    super(left, right);
    this.operator = operator;
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    T leftResult = operands()[0].evaluate(adapter, currentValue);
    T rightResult = operands()[1].evaluate(adapter, currentValue);
    JmesPathType leftType = adapter.typeOf(leftResult);
    JmesPathType rightType = adapter.typeOf(rightResult);
    if (leftType == JmesPathType.NUMBER && rightType == JmesPathType.NUMBER) {
      return compareNumbers(adapter, leftResult, rightResult);
    } else {
      return compareObjects(adapter, leftResult, rightResult);
    }
  }

  private <T> T compareObjects(Adapter<T> adapter, T leftResult, T rightResult) {
    int result = adapter.compare(leftResult, rightResult);
    if (operator.equals("==")) {
      return adapter.createBoolean(result == 0);
    } else if (operator.equals("!=")) {
      return adapter.createBoolean(result != 0);
    }
    return adapter.createNull();
  }

  private <T> T compareNumbers(Adapter<T> adapter, T leftResult, T rightResult) {
    int result = adapter.compare(leftResult, rightResult);
    if (operator.equals("==")) {
      return adapter.createBoolean(result == 0);
    } else if (operator.equals("!=")) {
      return adapter.createBoolean(result != 0);
    } else if (operator.equals(">")) {
      return adapter.createBoolean(result > 0);
    } else if (operator.equals(">=")) {
      return adapter.createBoolean(result >= 0);
    } else if (operator.equals("<")) {
      return adapter.createBoolean(result < 0);
    } else if (operator.equals("<=")) {
      return adapter.createBoolean(result <= 0);
    }
    return adapter.createNull();
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
