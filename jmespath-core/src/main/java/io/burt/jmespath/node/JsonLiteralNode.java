package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode<T> extends Node<T> {
  private final String raw;

  public JsonLiteralNode(Adapter<T> runtime, String raw) {
    super(runtime);
    this.raw = raw;
  }

  @Override
  public T search(T input) {
    return runtime.parseString(raw());
  }

  protected String raw() {
    return raw;
  }

  @Override
  protected String internalToString() {
    return String.format("%s", raw());
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    JsonLiteralNode<T> other = (JsonLiteralNode<T>) o;
    return raw().equals(other.raw());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + raw.hashCode();
    return h;
  }
}
