package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class IndexNode<T> extends Node<T> {
  private final int index;

  public IndexNode(Adapter<T> runtime, int index) {
    super(runtime);
    this.index = index;
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.ARRAY) {
      List<T> elements = runtime.toList(input);
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
  protected boolean internalEquals(Object o) {
    IndexNode<?> other = (IndexNode<?>) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
