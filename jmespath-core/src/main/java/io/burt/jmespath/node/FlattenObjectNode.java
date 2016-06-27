package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class FlattenObjectNode extends JmesPathNode {
  public FlattenObjectNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    if (adapter.typeOf(currentValue) == JmesPathType.OBJECT) {
      return adapter.createArray(adapter.toList(currentValue));
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
