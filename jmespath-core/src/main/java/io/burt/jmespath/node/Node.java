package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public abstract class Node<T> implements Expression<T> {
  protected final Adapter<T> runtime;
  private final Node<T> source;

  public Node(Adapter<T> runtime) {
    this(runtime, new CurrentNode<T>(runtime));
  }

  public Node(Adapter<T> runtime, Node<T> source) {
    this.runtime = runtime;
    this.source = source;
  }

  public abstract Node<T> copyWithSource(Node<T> source);

  public T search(T input) {
    return searchWithCurrentValue(source().search(input));
  }

  protected T searchWithCurrentValue(T currentValue) {
    return currentValue;
  }

  public Node<T> source() {
    return source;
  }

  @Override
  public String toString() {
    String extraArgs = internalToString();
    StringBuilder str = new StringBuilder();
    String name = getClass().getName();
    str.append(name.substring(name.lastIndexOf(".") + 1));
    str.delete(str.length() - 4, str.length());
    str.append("(");
    if (extraArgs != null) {
      str.append(extraArgs);
      if (extraArgs.length() > 0 && !extraArgs.endsWith(", ")) {
        str.append(", ");
      }
    }
    str.append(source());
    str.append(")");
    return str.toString();
  }

  protected String internalToString() {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!getClass().isInstance(o)) {
      return false;
    }
    Node<?> other = (Node<?>) o;
    return internalEquals(o) && (source() == other.source() || (source() != null && other.source() != null && source().equals(other.source())));
  }

  abstract protected boolean internalEquals(Object o);

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + internalHashCode();
    h = h * 31 + (source() == null ? 0 : source().hashCode());
    return h;
  }

  abstract protected int internalHashCode();
}
