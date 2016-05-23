package io.burt.jmespath.ast;

import java.util.Arrays;

public class CreateListNode extends JmesPathNode {
  private final JmesPathNode[] entries;

  public CreateListNode(JmesPathNode[] entries, JmesPathNode source) {
    super(source);
    this.entries = entries;
  }

  protected JmesPathNode[] entries() {
    return entries;
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
    CreateListNode other = (CreateListNode) o;
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
