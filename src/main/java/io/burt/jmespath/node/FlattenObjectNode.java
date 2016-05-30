package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

public class FlattenObjectNode extends JmesPathNode {
  public FlattenObjectNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    if (adapter.isObject(currentValue)) {
      return adapter.createArray(adapter.toList(currentValue), true);
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
