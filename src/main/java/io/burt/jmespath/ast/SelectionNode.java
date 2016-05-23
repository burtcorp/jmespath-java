package io.burt.jmespath.ast;

public class SelectionNode extends JmesPathNode {
  private final JmesPathNode test;

  public SelectionNode(JmesPathNode test, JmesPathNode source) {
    super(source);
    this.test = test;
  }

  protected JmesPathNode test() {
    return test;
  }

  @Override
  protected String internalToString() {
    return test.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    SelectionNode other = (SelectionNode) o;
    return test().equals(other.test());
  }

  @Override
  protected int internalHashCode() {
    return test().hashCode();
  }
}
