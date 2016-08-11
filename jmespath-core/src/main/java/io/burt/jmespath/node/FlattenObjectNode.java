package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class FlattenObjectNode<T> extends Node<T> {
  public FlattenObjectNode(Adapter<T> runtime, Node<T> source) {
    super(runtime, source);
  }

  @Override
  protected T searchOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.OBJECT) {
      return runtime.createArray(runtime.toList(currentValue));
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
