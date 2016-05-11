package io.burt.jmespath.ast;

public class AndNode extends SequenceNode {
  public AndNode(JmesPathNode... sequence) {
    super(sequence);
  }
}
