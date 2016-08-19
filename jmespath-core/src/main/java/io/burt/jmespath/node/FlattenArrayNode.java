package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class FlattenArrayNode<T> extends Node<T> {
  public FlattenArrayNode(Adapter<T> runtime, Node<T> source) {
    super(runtime, source);
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new FlattenArrayNode<T>(runtime, source);
  }

  @Override
  protected T searchWithCurrentValue(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      List<T> elements = runtime.toList(currentValue);
      List<T> flattened = new LinkedList<>();
      for (T element : elements) {
        if (runtime.typeOf(element) == JmesPathType.ARRAY) {
          flattened.addAll(runtime.toList(element));
        } else {
          flattened.add(element);
        }
      }
      return runtime.createArray(flattened);
    } else {
      return runtime.createNull();
    }
  }

  @Override
  protected boolean internalEquals(Object o) {
    return true;
  }

  @Override
  protected int internalHashCode() {
    return 19;
  }
}
