package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class FlattenObjectNode extends JmesPathNode {
  public FlattenObjectNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.OBJECT) {
      return runtime.createArray(runtime.toList(currentValue));
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
