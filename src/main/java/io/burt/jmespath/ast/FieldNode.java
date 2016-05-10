package io.burt.jmespath.ast;

public class FieldNode extends JmesPathNode {
  private final String field;

  public FieldNode(String field) {
    this.field = field;
  }

  protected String field() {
    return field;
  }

  @Override
  public String toString() {
    return String.format("FieldNode(%s)", field);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FieldNode)) {
      return false;
    }
    FieldNode other = (FieldNode) o;
    return this.field().equals(other.field());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + field.hashCode();
    return h;
  }
}
