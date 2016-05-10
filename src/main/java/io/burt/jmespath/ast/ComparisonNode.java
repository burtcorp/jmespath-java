package io.burt.jmespath.ast;

public class ComparisonNode extends JmesPathNode {
  private final String operator;
  private final JmesPathNode left;
  private final JmesPathNode right;

  public ComparisonNode(String operator, JmesPathNode left, JmesPathNode right) {
    this.operator = operator;
    this.left = left;
    this.right = right;
  }

  protected String operator() {
    return operator;
  }

  protected JmesPathNode left() {
    return left;
  }

  protected JmesPathNode right() {
    return right;
  }

  @Override
  public String toString() {
    return String.format("ComparisonNode(%s, %s, %s)", operator, left, right);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ComparisonNode)) {
      return false;
    }
    ComparisonNode other = (ComparisonNode) o;
    return this.operator().equals(other.operator())
        && this.left().equals(other.left())
        && this.right().equals(other.right());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + operator.hashCode();
    h = h * 31 + left.hashCode();
    h = h * 31 + right.hashCode();
    return h;
  }
}
