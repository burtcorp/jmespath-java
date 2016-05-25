package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class JoinNode extends JmesPathNode {
  public JoinNode(JmesPathNode source) {
    super(source);
  }

  @Override
  public boolean isProjection() {
    return false;
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T currentValue) {
    return source().evaluate(adapter, currentValue);
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
