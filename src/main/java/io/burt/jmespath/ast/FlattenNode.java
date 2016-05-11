package io.burt.jmespath.ast;

public class FlattenNode extends WrapperNode {
  public FlattenNode(JmesPathNode expression) {
    super(expression);
  }

  public FlattenNode() {
    this(null);
  }
}
