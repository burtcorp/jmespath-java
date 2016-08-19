package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class IndexNode<T> extends Node<T> {
  private final int index;

  public IndexNode(Adapter<T> runtime, int index, Node<T> source) {
    super(runtime, source);
    this.index = index;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new IndexNode<T>(runtime, index, source);
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
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
  protected boolean internalEquals(Object o) {
    IndexNode<?> other = (IndexNode<?>) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
