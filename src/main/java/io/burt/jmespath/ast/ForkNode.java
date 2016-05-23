package io.burt.jmespath.ast;

public class ForkNode extends JmesPathNode {
  public ForkNode(JmesPathNode source) {
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
