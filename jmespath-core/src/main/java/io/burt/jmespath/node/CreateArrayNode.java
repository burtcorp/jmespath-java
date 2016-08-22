package io.burt.jmespath.node;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends Node<T> {
  private final List<Expression<T>> entries;

  public CreateArrayNode(Adapter<T> runtime, List<? extends Expression<T>> entries) {
    super(runtime);
    this.entries = new ArrayList<>(entries);
  }

  protected List<Expression<T>> entries() {
    return entries;
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.NULL) {
      return input;
    } else {
      List<T> array = new ArrayList<>();
      for (Expression<T> entry : entries) {
        array.add(entry.search(input));
      }
      return runtime.createArray(array);
    }
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("[");
    Iterator<Expression<T>> entryIterator = entries.iterator();
    while (entryIterator.hasNext()) {
      Expression<T> entry = entryIterator.next();
      str.append(entry);
      if (entryIterator.hasNext()) {
        str.append(", ");
      }
    }
    str.append(']');
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateArrayNode<?> other = (CreateArrayNode<?>) o;
    return entries().equals(other.entries());
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
