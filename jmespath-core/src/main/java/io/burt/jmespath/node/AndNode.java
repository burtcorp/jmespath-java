package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class AndNode<T> extends OperatorNode<T> {
  public AndNode(Adapter<T> adapter, Expression<T> left, Expression<T> right) {
    super(adapter, left, right);
  }

  @Override
  public T search(T input) {
    T leftResult = operand(0).search(input);
    if (runtime.isTruthy(leftResult)) {
      return operand(1).search(input);
    } else {
      return leftResult;
    }
  }
}
