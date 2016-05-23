package io.burt.jmespath.ast;

public class NegateNode extends JmesPathNode {
  public NegateNode(JmesPathNode source) {
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
