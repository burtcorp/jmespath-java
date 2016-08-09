package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;

public class OrNode extends OperatorNode {
  public OrNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    T leftResult = operands()[0].evaluate(runtime, currentValue);
    if (runtime.isTruthy(leftResult)) {
      return leftResult;
    } else {
      return operands()[1].evaluate(runtime, currentValue);
    }
  }
}
