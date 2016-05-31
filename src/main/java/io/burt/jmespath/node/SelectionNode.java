package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

public class SelectionNode extends JmesPathNode {
  private final JmesPathNode test;

  public SelectionNode(JmesPathNode test, JmesPathNode source) {
    super(source);
    this.test = test;
  }

  @Override
  public <T> T evaluateOne(Adapter<T> adapter, T projectionElement) {
    if (adapter.isArray(projectionElement)) {
      List<T> selectedElements = new LinkedList<>();
      for (T element : adapter.toList(projectionElement)) {
        T testResult = test().evaluate(adapter, element);
        if (adapter.isTruthy(testResult)) {
          selectedElements.add(element);
        }
      }
      return adapter.createArray(selectedElements, true);
    } else {
      return adapter.createNull();
    }
  }

  protected JmesPathNode test() {
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
