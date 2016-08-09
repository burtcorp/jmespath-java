package io.burt.jmespath.node;

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
  public <T> T evaluate(Adapter<T> runtime, T input) {
    return source() == null ? input : super.evaluate(runtime, input);
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
