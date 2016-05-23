package io.burt.jmespath.ast;

public class FlattenObjectNode extends JmesPathNode {
  public FlattenObjectNode() {
    super();
  }

  public FlattenObjectNode(JmesPathNode source) {
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
