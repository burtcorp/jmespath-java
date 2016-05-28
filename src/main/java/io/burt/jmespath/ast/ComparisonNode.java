package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class ComparisonNode extends OperatorNode {
  private final String operator;

  public ComparisonNode(String operator, JmesPathNode left, JmesPathNode right) {
    super(left, right);
    this.operator = operator;
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    T leftResult = operands()[0].evaluate(adapter, currentValue);
    T rightResult = operands()[1].evaluate(adapter, currentValue);
    if (adapter.isNumber(leftResult) && adapter.isNumber(rightResult)) {
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
    return operator;
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
