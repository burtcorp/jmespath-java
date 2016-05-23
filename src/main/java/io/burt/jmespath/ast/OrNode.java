package io.burt.jmespath.ast;

public class OrNode extends OperatorNode {
  public OrNode(JmesPathNode left, JmesPathNode right) {
    super(left, right);
  }
}
