package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends JmesPathNode<T> {
  private final List<JmesPathNode<T>> entries;

  public CreateArrayNode(Adapter<T> runtime, List<JmesPathNode<T>> entries, JmesPathNode<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  protected List<JmesPathNode<T>> entries() {
    return entries;
  }

  @Override
  protected T searchOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      List<T> array = new ArrayList<>();
      for (JmesPathNode<T> entry : entries) {
        array.add(entry.search(currentValue));
      }
      return runtime.createArray(array);
    }
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("[");
    Iterator<JmesPathNode<T>> entryIterator = entries.iterator();
    while (entryIterator.hasNext()) {
      JmesPathNode<T> entry = entryIterator.next();
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
    for (JmesPathNode<T> node : entries) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
