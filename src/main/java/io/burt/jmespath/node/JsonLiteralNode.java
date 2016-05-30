package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class JsonLiteralNode extends JmesPathNode {
  private final String raw;
  private final Object tree;

  public JsonLiteralNode(String raw, Object tree) {
    this.raw = raw;
    this.tree = tree;
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    return adapter.parseString(raw());
  }

  protected String raw() {
    return raw;
  }

  protected Object tree() {
    return tree;
  }

  @Override
  protected String internalToString() {
    return String.format("%s, %s", raw, tree);
  }

  @Override
  protected boolean internalEquals(Object o) {
    JsonLiteralNode other = (JsonLiteralNode) o;
    return raw().equals(other.raw()) && tree().equals(other.tree());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + raw.hashCode();
    h = h * 31 + tree.hashCode();
    return h;
  }
}
