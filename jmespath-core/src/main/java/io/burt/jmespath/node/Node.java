package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public abstract class Node<T> implements Expression<T> {
  protected final Adapter<T> runtime;

  public Node(Adapter<T> runtime) {
    this.runtime = runtime;
  }

  @Override
  public abstract T search(T input);

  @Override
  public String toString() {
    String extraArgs = internalToString();
    StringBuilder str = new StringBuilder();
    String name = getClass().getName();
    str.append(name.substring(name.lastIndexOf('.') + 1));
    str.setLength(str.length() - 4);
    str.append('(');
    if (extraArgs != null) {
      str.append(extraArgs);
    }
    str.append(')');
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
    return internalEquals(o);
  }

  protected abstract boolean internalEquals(Object o);

  @Override
  public int hashCode() {
    return internalHashCode();
  }

  protected abstract int internalHashCode();
}
