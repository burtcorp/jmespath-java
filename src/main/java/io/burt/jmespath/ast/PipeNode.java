package io.burt.jmespath.ast;

public class PipeNode extends SequenceNode {
  public PipeNode(JmesPathNode... sequence) {
    super(sequence);
  }
}
