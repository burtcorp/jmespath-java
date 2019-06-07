package io.burt.jmespath.node;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends Node<T> {
  private final List<Expression<T>> entries;

  public CreateArrayNode(Adapter<T> runtime, List<? extends Expression<T>> entries) {
    super(runtime);
    this.entries = new ArrayList<>(entries);
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.NULL) {
      return input;
    } else {
      List<T> array = new ArrayList<>(entries.size());
      for (Expression<T> entry : entries) {
        array.add(entry.search(input));
      }
      return runtime.createArray(array);
    }
  }

  @Override
  protected String internalToString() {
    if (entries.isEmpty()) {
      return "[]";
    }
    StringBuilder str = new StringBuilder("[");
    for (Expression<T> entry : entries) {
      str.append(entry).append(", ");
    }
    str.setLength(str.length() - 2);
    return str.append(']').toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateArrayNode<?> other = (CreateArrayNode<?>) o;
    return entries.equals(other.entries);
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (Expression<T> node : entries) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
