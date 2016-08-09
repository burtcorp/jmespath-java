package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class ForkNode extends JmesPathNode {
  public ForkNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected boolean isProjection() {
    return true;
  }

  @Override
  protected <T> T evaluateWithCurrentValue(JmesPathRuntime<T> runtime, T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      return super.evaluateWithCurrentValue(runtime, currentValue);
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
