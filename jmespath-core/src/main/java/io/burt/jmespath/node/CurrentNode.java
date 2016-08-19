package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class CurrentNode<T> extends Node<T> {
  public CurrentNode(Adapter<T> runtime) {
    super(runtime, null);
  }

  public CurrentNode(Adapter<T> runtime, Node<T> source) {
    super(runtime, source);
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new CurrentNode<>(runtime, source);
  }

  @Override
  public T search(T input) {
    return source() == null ? input : super.search(input);
  }

  @Override
  public String toString() {
    if (source() == null) {
      return "Current()";
    } else {
      return super.toString();
    }
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
