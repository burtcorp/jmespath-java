package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class NegateNode<T> extends JmesPathNode<T> {
  public NegateNode(Adapter<T> runtime, JmesPathNode<T> source) {
    super(runtime, source);
  }

  @Override
  protected T evaluateOne(T currentValue) {
    return runtime.createBoolean(!runtime.isTruthy(currentValue));
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
