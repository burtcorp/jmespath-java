package io.burt.jmespath.ast;

public class ComparisonNode extends OperatorNode {
  private final String operator;

  public ComparisonNode(String operator, JmesPathNode left, JmesPathNode right) {
    super(left, right);
    this.operator = operator;
  }

  protected String operator() {
    return operator;
  }

  @Override
  protected String internalToString() {
    return operator;
  }

  @Override
  protected boolean internalEquals(Object o) {
    ComparisonNode other = (ComparisonNode) o;
    return operator().equals(other.operator());
  }

  @Override
  protected int internalHashCode() {
    return operator.hashCode();
  }
}
