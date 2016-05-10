package io.burt.jmespath.ast;

public class ChainNode extends SequenceNode {
  public ChainNode(JmesPathNode... chain) {
    super(chain);
  }
}
