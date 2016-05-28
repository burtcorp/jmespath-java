package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class AndNode extends OperatorNode {
  public AndNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    T currentValue = source().evaluate(adapter, input);
    T leftResult = operands()[0].evaluate(adapter, currentValue);
    if (adapter.isTruthy(leftResult)) {
      return operands()[1].evaluate(adapter, currentValue);
    } else {
      return leftResult;
    }
  }
}
