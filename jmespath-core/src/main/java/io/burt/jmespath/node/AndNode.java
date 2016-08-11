package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class AndNode<T> extends OperatorNode<T> {
  public AndNode(Adapter<T> adapter, Expression<T> left, Expression<T> right) {
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
