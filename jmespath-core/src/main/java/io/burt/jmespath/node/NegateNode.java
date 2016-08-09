package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class NegateNode extends JmesPathNode {
  public NegateNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> runtime, T currentValue) {
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
