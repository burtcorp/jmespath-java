package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class OrNode<T> extends OperatorNode<T> {
  public OrNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
    super(runtime, left, right);
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return this;
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
    T leftResult = operand(0).search(currentValue);
    if (runtime.isTruthy(leftResult)) {
      return leftResult;
    } else {
      return operand(1).search(currentValue);
    }
  }
}
