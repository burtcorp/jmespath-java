package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class SelectionNode extends JmesPathNode {
  private final JmesPathNode test;

  public SelectionNode(JmesPathNode test, JmesPathNode source) {
    super(source);
    this.test = test;
  }

  @Override
  public <T> T evaluateOne(JmesPathRuntime<T> runtime, T projectionElement) {
    if (runtime.typeOf(projectionElement) == JmesPathType.ARRAY) {
      List<T> selectedElements = new LinkedList<>();
      for (T element : runtime.toList(projectionElement)) {
        T testResult = test().evaluate(runtime, element);
        if (runtime.isTruthy(testResult)) {
          selectedElements.add(element);
        }
      }
      return runtime.createArray(selectedElements);
    } else {
      return runtime.createNull();
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
