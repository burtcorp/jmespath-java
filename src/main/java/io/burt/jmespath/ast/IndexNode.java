package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class IndexNode extends JmesPathNode {
  private final int index;

  public IndexNode(int index, JmesPathNode source) {
    super(source);
    this.index = index;
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T currentValue) {
    T input = source().evaluate(adapter, currentValue);
    return adapter.getIndex(input, index());
  }

  protected int index() {
    return index;
  }

  @Override
  protected String internalToString() {
    return String.valueOf(index);
  }

  @Override
  protected boolean internalEquals(Object o) {
    IndexNode other = (IndexNode) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
