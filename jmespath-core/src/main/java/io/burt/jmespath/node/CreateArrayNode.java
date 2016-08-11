package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends Node<T> {
  private final List<Expression<T>> entries;

  public CreateArrayNode(Adapter<T> runtime, List<Expression<T>> entries, Node<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  protected List<Expression<T>> entries() {
    return entries;
  }

  @Override
  protected T searchOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      List<T> array = new ArrayList<>();
      for (Expression<T> entry : entries) {
        array.add(entry.search(currentValue));
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
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateArrayNode other = (CreateArrayNode) o;
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
