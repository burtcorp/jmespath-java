package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class StringNode<T> extends Node<T> {
  private final String string;

  public StringNode(Adapter<T> runtime, String string) {
    super(runtime);
    this.string = string;
  }

  @Override
  public T search(T input) {
    return runtime.createString(string);
  }

  @Override
  public String toString() {
    return String.format("String(%s)", string);
  }

  @Override
  protected boolean internalEquals(Object o) {
    StringNode<?> other = (StringNode<?>) o;
    return string.equals(other.string);
  }

  @Override
  protected int internalHashCode() {
    return string.hashCode();
  }
}
