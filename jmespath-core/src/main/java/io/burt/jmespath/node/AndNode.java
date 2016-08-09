package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class AndNode<T> extends OperatorNode<T> {
  public AndNode(Adapter<T> adapter, JmesPathNode<T> left, JmesPathNode<T> right) {
    super(adapter, left, right);
  }

  @Override
  protected T searchOne(T currentValue) {
    T leftResult = operand(0).search(currentValue);
    if (runtime.isTruthy(leftResult)) {
      return operand(1).search(currentValue);
    } else {
      return leftResult;
    }
  }
}
