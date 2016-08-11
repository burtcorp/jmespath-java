package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends Node<T> {
  private final List<Node<T>> entries;

  public CreateArrayNode(Adapter<T> runtime, List<Node<T>> entries, Node<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  protected List<Node<T>> entries() {
    return entries;
  }

  @Override
  protected T searchOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      List<T> array = new ArrayList<>();
      for (Node<T> entry : entries) {
        array.add(entry.search(currentValue));
      }
      return runtime.createArray(array);
    }
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("[");
    Iterator<Node<T>> entryIterator = entries.iterator();
    while (entryIterator.hasNext()) {
      Node<T> entry = entryIterator.next();
      str.append(entry);
      if (entryIterator.hasNext()) {
        str.append(", ");
      }
    }
    str.append("]");
    return str.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    CreateArrayNode<T> other = (CreateArrayNode<T>) o;
    return entries().equals(other.entries());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (Node<T> node : entries) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
