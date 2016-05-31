package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode extends JmesPathNode {
  private final String raw;

  public JsonLiteralNode(String raw) {
    this.raw = raw;
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    return adapter.parseString(raw());
  }

  protected String raw() {
    return raw;
  }

  @Override
  protected String internalToString() {
    return String.format("%s", raw());
  }

  @Override
  protected boolean internalEquals(Object o) {
    JsonLiteralNode other = (JsonLiteralNode) o;
    return raw().equals(other.raw());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + raw.hashCode();
    return h;
  }
}
