package io.burt.jmespath.ast;

public class OrNode extends SequenceNode {
  public OrNode(JmesPathNode... sequence) {
    super(sequence);
  }
}
