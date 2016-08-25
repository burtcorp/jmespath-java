package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode<T> extends Node<T> {
  private final String rawJson;
  private final T json;

  public JsonLiteralNode(Adapter<T> runtime, String rawJson) {
    super(runtime);
    this.rawJson = rawJson;
    this.json = runtime.parseString(rawJson);
  }

  @Override
  public T search(T input) {
    return json;
  }

  @Override
  protected String internalToString() {
    return String.format("%s", rawJson);
  }

  @Override
  protected boolean internalEquals(Object o) {
    JsonLiteralNode<?> other = (JsonLiteralNode<?>) o;
    return json.equals(other.json);
  }

  @Override
  protected int internalHashCode() {
    return json.hashCode();
  }
}
