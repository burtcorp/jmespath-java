package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ForkNode<T> extends JmesPathNode<T> {
  public ForkNode(Adapter<T> runtime, JmesPathNode<T> source) {
    super(runtime, source);
  }

  @Override
  protected boolean isProjection() {
    return true;
  }

  @Override
  protected T evaluateWithCurrentValue(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      return super.evaluateWithCurrentValue(currentValue);
    } else {
      return runtime.createNull();
    }
  }

  @Override
  protected boolean internalEquals(Object o) {
    return true;
  }

  @Override
  protected int internalHashCode() {
    return 19;
  }
}
