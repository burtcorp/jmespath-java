package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class SelectionNode<T> extends Node<T> {
  private final Node<T> test;

  public SelectionNode(Adapter<T> runtime, Node<T> test, Node<T> source) {
    super(runtime, source);
    this.test = test;
  }

  @Override
  public T searchOne(T projectionElement) {
    if (runtime.typeOf(projectionElement) == JmesPathType.ARRAY) {
      List<T> selectedElements = new LinkedList<>();
      for (T element : runtime.toList(projectionElement)) {
        T testResult = test().search(element);
        if (runtime.isTruthy(testResult)) {
          selectedElements.add(element);
        }
      }
      return runtime.createArray(selectedElements);
    } else {
      return runtime.createNull();
    }
  }

  protected Node<T> test() {
    return test;
  }

  @Override
  protected String internalToString() {
    return test.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    SelectionNode<T> other = (SelectionNode<T>) o;
    return test().equals(other.test());
  }

  @Override
  protected int internalHashCode() {
    return test().hashCode();
  }
}
