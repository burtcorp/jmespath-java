package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class StringNode extends JmesPathNode {
  private final String string;

  public StringNode(String string) {
    super();
    this.string = string;
  }

  @Override
  public <T> T evaluate(Adapter<T> runtime, T input) {
    return runtime.createString(string());
  }

  protected String string() {
    return string;
  }

  @Override
  public String toString() {
    return String.format("String(%s)", string);
  }

  @Override
  protected boolean internalEquals(Object o) {
    StringNode other = (StringNode) o;
    return string().equals(other.string());
  }

  @Override
  protected int internalHashCode() {
    return string.hashCode();
  }
}
