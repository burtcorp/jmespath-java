package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class NegateNode extends JmesPathNode {
  public NegateNode(JmesPathNode source) {
    super(source);
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    T currentValue = source().evaluate(adapter, input);
    return adapter.createBoolean(!adapter.isTruthy(currentValue));
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
