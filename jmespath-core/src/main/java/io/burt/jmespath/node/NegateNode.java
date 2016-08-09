package io.burt.jmespath.node;

import io.burt.jmespath.JmesPathRuntime;

public class NegateNode extends JmesPathNode {
  public NegateNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    return runtime.createBoolean(!runtime.isTruthy(currentValue));
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
