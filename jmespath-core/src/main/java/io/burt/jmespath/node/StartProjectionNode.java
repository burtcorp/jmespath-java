package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class StartProjectionNode<T> extends Node<T> {
  public StartProjectionNode(Adapter<T> runtime, Node<T> source) {
    super(runtime, source);
  }

  @Override
  protected boolean isProjection() {
    return true;
  }

  @Override
  protected int projectionLevel() {
    return source().projectionLevel() + 1;
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      return super.searchWithCurrentValue(currentValue);
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
