package io.burt.jmespath.ast;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class CreateArrayNode extends ProjectionNode {
  private final JmesPathNode[] entries;

  public CreateArrayNode(JmesPathNode[] entries, JmesPathNode source) {
    super(source);
    this.entries = entries;
  }

  protected JmesPathNode[] entries() {
    return entries;
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    List<T> array = new ArrayList<>();
    for (JmesPathNode entry : entries) {
      array.add(entry.evaluate(adapter, currentValue));
    }
    return adapter.createArray(array);
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("[");
    for (JmesPathNode entry : entries) {
      str.append(entry).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateArrayNode other = (CreateArrayNode) o;
    return Arrays.equals(entries(), other.entries());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (JmesPathNode node : entries) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
