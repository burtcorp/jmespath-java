package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class StringNode<T> extends Node<T> {
  private final String rawString;
  private final T string;

  public StringNode(Adapter<T> runtime, String rawString) {
    super(runtime);
    this.rawString = rawString;
    this.string = runtime.createString(rawString);
  }

  @Override
  public T search(T input) {
    return string;
  }

  @Override
  protected String internalToString() {
    return rawString;
  }

  @Override
  protected boolean internalEquals(Object o) {
    StringNode<?> other = (StringNode<?>) o;
    return rawString.equals(other.rawString);
  }

  @Override
  protected int internalHashCode() {
    return rawString.hashCode();
  }
}
