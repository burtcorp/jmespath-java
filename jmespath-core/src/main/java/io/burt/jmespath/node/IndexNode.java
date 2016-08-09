package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class IndexNode extends JmesPathNode {
  private final int index;

  public IndexNode(int index, JmesPathNode source) {
    super(source);
    this.index = index;
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
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
    IndexNode other = (IndexNode) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
