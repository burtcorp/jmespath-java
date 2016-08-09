package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class StringNode<T> extends JmesPathNode<T> {
  private final String string;

  public StringNode(Adapter<T> runtime, String string) {
    super(runtime);
    this.string = string;
  }

  @Override
  public T search(T input) {
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
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    StringNode<T> other = (StringNode<T>) o;
    return string().equals(other.string());
  }

  @Override
  protected int internalHashCode() {
    return string.hashCode();
  }
}
