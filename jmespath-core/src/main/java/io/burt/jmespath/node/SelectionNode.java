package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class SelectionNode<T> extends Node<T> {
  private final Expression<T> test;

  public SelectionNode(Adapter<T> runtime, Expression<T> test) {
    super(runtime);
    this.test = test;
  }

  public Expression<T> test() {
    return test;
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.ARRAY) {
      List<T> selectedElements = new LinkedList<>();
      for (T element : runtime.toList(input)) {
        T testResult = test.search(element);
        if (runtime.isTruthy(testResult)) {
          selectedElements.add(element);
        }
      }
      return runtime.createArray(selectedElements);
    } else {
      return runtime.createNull();
    }
  }

  @Override
  protected String internalToString() {
    return test.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    SelectionNode<?> other = (SelectionNode<?>) o;
    return test.equals(other.test);
  }

  @Override
  protected int internalHashCode() {
    return test.hashCode();
  }
}
