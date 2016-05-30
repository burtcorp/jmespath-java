package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class ForkNode extends JmesPathNode {
  public ForkNode(JmesPathNode source) {
    super(source);
  }

  @Override
  public boolean isProjection() {
    return true;
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    if (adapter.isArray(currentValue)) {
      return super.evaluateWithCurrentValue(adapter, currentValue);
    } else {
      return adapter.createNull();
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
