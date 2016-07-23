package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
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
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    if (adapter.typeOf(currentValue) == JmesPathType.ARRAY) {
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
