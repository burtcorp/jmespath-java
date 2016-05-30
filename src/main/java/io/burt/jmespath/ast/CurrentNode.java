package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class CurrentNode extends JmesPathNode {
  public CurrentNode() {
    super(null);
  }

  public CurrentNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected boolean isProjection() {
    return source() == null ? false : super.isProjection();
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    return source() == null ? input : super.evaluate(adapter, input);
  }

  @Override
  public boolean internalEquals(Object o) {
    return true;
  }

  @Override
  protected int internalHashCode() {
    return 17;
  }
}
