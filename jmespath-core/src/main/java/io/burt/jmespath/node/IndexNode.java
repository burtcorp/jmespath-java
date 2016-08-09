package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class IndexNode<T> extends JmesPathNode<T> {
  private final int index;

  public IndexNode(Adapter<T> runtime, int index, JmesPathNode<T> source) {
    super(runtime, source);
    this.index = index;
  }

  @Override
  protected T searchOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      List<T> elements = runtime.toList(currentValue);
      int i = index();
      if (i < 0) {
        i = elements.size() + i;
      }
      if (i >= 0 && i < elements.size()) {
        return elements.get(i);
      }
    }
    return runtime.createNull();
  }

  protected int index() {
    return index;
  }

  @Override
  protected String internalToString() {
    return String.valueOf(index);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    IndexNode<T> other = (IndexNode<T>) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
