package io.burt.jmespath.node;

public class JoinNode extends JmesPathNode {
  public JoinNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected boolean isProjection() {
    return false;
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
