package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class NegateNode<T> extends Node<T> {
  public NegateNode(Adapter<T> runtime, Node<T> source) {
    super(runtime, source);
  }

  @Override
  protected T searchOne(T currentValue) {
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
