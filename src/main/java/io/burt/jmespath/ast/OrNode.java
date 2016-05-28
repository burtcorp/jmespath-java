package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class OrNode extends OperatorNode {
  public OrNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    T currentValue = source().evaluate(adapter, input);
    T leftResult = operands()[0].evaluate(adapter, currentValue);
    if (adapter.isTruthy(leftResult)) {
      return leftResult;
    } else {
      return operands()[1].evaluate(adapter, currentValue);
    }
  }
}
