package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode<T> extends Node<T> {
  private final String raw;
  private final T tree;

  public JsonLiteralNode(Adapter<T> runtime, String raw, T tree) {
    super(runtime);
    this.raw = raw;
    this.tree = tree;
  }

  @Override
  public T search(T input) {
    return tree;
  }

  @Override
  protected String internalToString() {
    return String.format("%s", raw);
  }

  @Override
  protected boolean internalEquals(Object o) {
    JsonLiteralNode<?> other = (JsonLiteralNode<?>) o;
    return tree.equals(other.tree);
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + tree.hashCode();
    return h;
  }
}
