package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;

public class IndexNode extends JmesPathNode {
  private final int index;

  public IndexNode(int index, JmesPathNode source) {
    super(source);
    this.index = index;
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    if (adapter.isArray(currentValue)) {
      List<T> elements = adapter.toList(currentValue);
      int i = index();
      if (i < 0) {
        i = elements.size() + i;
      }
      if (i >= 0 && i < elements.size()) {
        return elements.get(i);
      }
    }
    return adapter.createNull();
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
