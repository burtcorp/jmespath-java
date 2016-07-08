package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class ParsedJsonLiteralNode extends JsonLiteralNode {
  private final Object tree;

  public ParsedJsonLiteralNode(String raw, Object tree) {
    super(raw);
    this.tree = tree;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T evaluate(Adapter<T> adapter, T input) {
    return (T) tree();
  }

  protected Object tree() {
    return tree;
  }

  @Override
  protected String internalToString() {
    return String.format("%s", tree());
  }

  @Override
  protected boolean internalEquals(Object o) {
    if (o instanceof ParsedJsonLiteralNode) {
      ParsedJsonLiteralNode other = (ParsedJsonLiteralNode) o;
      return tree().equals(other.tree());
    } else {
      return super.internalEquals(o);
    }
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + tree().hashCode();
    return h;
  }
}
