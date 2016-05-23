package io.burt.jmespath.ast;

public class IndexProjectionNode extends JmesPathNode {
  private final int index;

  public IndexProjectionNode(int index, JmesPathNode source) {
    super(source);
    this.index = index;
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
    IndexProjectionNode other = (IndexProjectionNode) o;
    return index() == other.index();
  }

  @Override
  protected int internalHashCode() {
    return index;
  }
}
