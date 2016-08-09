package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class CurrentNode<T> extends JmesPathNode<T> {
  public CurrentNode(Adapter<T> runtime) {
    super(runtime, null);
  }

  public CurrentNode(Adapter<T> runtime, JmesPathNode<T> source) {
    super(runtime, source);
  }

  @Override
  protected boolean isProjection() {
    return source() == null ? false : super.isProjection();
  }

  @Override
  public T search(T input) {
    return source() == null ? input : super.search(input);
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
