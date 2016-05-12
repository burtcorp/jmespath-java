package io.burt.jmespath.ast;

public class JsonLiteralNode extends JmesPathNode {
  private final String raw;
  private final Object tree;

  public JsonLiteralNode(String raw, Object tree) {
    this.raw = raw;
    this.tree = tree;
  }

  protected String raw() {
    return raw;
  }

  protected Object tree() {
    return tree;
  }

  @Override
  public String toString() {
    return String.format("JsonLiteralNode(%s, %s)", raw, tree);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JsonLiteralNode)) {
      return false;
    }
    JsonLiteralNode other = (JsonLiteralNode) o;
    return this.raw().equals(other.raw()) && this.tree().equals(other.tree());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + raw.hashCode();
    h = h * 31 + tree.hashCode();
    return h;
  }
}
