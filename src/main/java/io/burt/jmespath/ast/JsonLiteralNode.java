package io.burt.jmespath.ast;

public class JsonLiteralNode extends JmesPathNode {
  private final String json;

  public JsonLiteralNode(String json) {
    this.json = json;
  }

  protected String json() {
    return json;
  }

  @Override
  public String toString() {
    return String.format("JsonLiteralNode(%s)", json);
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
    return this.json().equals(other.json());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + json.hashCode();
    return h;
  }
}
