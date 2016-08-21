package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class CurrentNode<T> extends Node<T> {
  public CurrentNode(Adapter<T> runtime) {
    super(runtime);
  }

  @Override
  public T search(T input) {
    return input;
  }

  @Override
  public boolean internalEquals(Object o) {
    return true;
  }

  @Override
  protected int internalHashCode() {
    return 17;
  }
}
