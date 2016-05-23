package io.burt.jmespath.ast;

public class FlattenListNode extends JmesPathNode {
  public FlattenListNode() {
    super();
  }

  public FlattenListNode(JmesPathNode source) {
    super(source);
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
