package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class OrNode<T> extends OperatorNode<T> {
  public OrNode(Adapter<T> runtime, JmesPathNode<T> left, JmesPathNode<T> right) {
    super(runtime, left, right);
  }

  @Override
  protected T evaluateOne(T currentValue) {
    T leftResult = operands()[0].evaluate(currentValue);
    if (runtime.isTruthy(leftResult)) {
      return leftResult;
    } else {
      return operands()[1].evaluate(currentValue);
    }
  }
}
