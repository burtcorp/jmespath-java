package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode<T> extends Node<T> {
  private final String rawValue;
  private final T value;

  public JsonLiteralNode(Adapter<T> runtime, String rawValue) {
    super(runtime);
    this.rawValue = rawValue;
    this.value = runtime.parseString(rawValue);
  }

  public String rawValue() {
    return rawValue;
  }

  @Override
  public T search(T input) {
    return value;
  }

  @Override
  protected String internalToString() {
    return rawValue;
  }

  @Override
  protected boolean internalEquals(Object o) {
    JsonLiteralNode<?> other = (JsonLiteralNode<?>) o;
    return rawValue.equals(other.rawValue);
  }

  @Override
  protected int internalHashCode() {
    return rawValue.hashCode();
  }
}
