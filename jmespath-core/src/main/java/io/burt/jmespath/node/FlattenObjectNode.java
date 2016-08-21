package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class FlattenObjectNode<T> extends Node<T> {
  public FlattenObjectNode(Adapter<T> runtime) {
    super(runtime);
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.OBJECT) {
      return runtime.createArray(runtime.toList(input));
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
