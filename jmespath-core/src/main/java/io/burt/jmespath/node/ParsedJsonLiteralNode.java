package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class ParsedJsonLiteralNode<T> extends JsonLiteralNode<T> {
  private final T tree;

  public ParsedJsonLiteralNode(Adapter<T> runtime, String raw, T tree) {
    super(runtime, raw);
    this.tree = tree;
  }

  @Override
  public T search(T input) {
    return tree();
  }

  protected T tree() {
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
