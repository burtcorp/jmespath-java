package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class SelectionNode<T> extends Node<T> {
  private final Expression<T> test;

  public SelectionNode(Adapter<T> runtime, Expression<T> test, Node<T> source) {
    super(runtime, source);
    this.test = test;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new SelectionNode<T>(runtime, test, source);
  }

  @Override
  public T searchWithCurrentValue(T projectionElement) {
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

  protected Expression<T> test() {
    return test;
  }

  @Override
  protected String internalToString() {
    return test.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    SelectionNode other = (SelectionNode) o;
    return test().equals(other.test());
  }

  @Override
  protected int internalHashCode() {
    return test().hashCode();
  }
}
