package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;

public class AndNode extends OperatorNode {
  public AndNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    T leftResult = operands()[0].evaluate(runtime, currentValue);
    if (runtime.isTruthy(leftResult)) {
      return operands()[1].evaluate(runtime, currentValue);
    } else {
      return leftResult;
    }
  }
}
