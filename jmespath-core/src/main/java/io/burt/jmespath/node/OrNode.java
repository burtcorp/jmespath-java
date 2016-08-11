package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class OrNode<T> extends OperatorNode<T> {
  public OrNode(Adapter<T> runtime, Node<T> left, Node<T> right) {
    super(runtime, left, right);
  }

  @Override
  protected T searchOne(T currentValue) {
    T leftResult = operand(0).search(currentValue);
    if (runtime.isTruthy(leftResult)) {
      return leftResult;
    } else {
      return operand(1).search(currentValue);
    }
  }
}
