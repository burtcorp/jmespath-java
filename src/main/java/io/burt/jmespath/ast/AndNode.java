package io.burt.jmespath.ast;

public class AndNode extends OperatorNode {
  public AndNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }
}
