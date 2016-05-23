package io.burt.jmespath.ast;

public class JoinNode extends JmesPathNode {
  public JoinNode(JmesPathNode source) {
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
