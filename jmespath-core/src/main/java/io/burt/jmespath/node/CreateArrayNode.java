package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode<T> extends JmesPathNode<T> {
  private final JmesPathNode<T>[] entries;

  public CreateArrayNode(Adapter<T> runtime, JmesPathNode<T>[] entries, JmesPathNode<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  protected JmesPathNode<T>[] entries() {
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
    for (JmesPathNode<T> entry : entries) {
      str.append(entry).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    CreateArrayNode<T> other = (CreateArrayNode<T>) o;
    return Arrays.equals(entries(), other.entries());
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
