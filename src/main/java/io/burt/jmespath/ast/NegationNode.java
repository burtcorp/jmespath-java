package io.burt.jmespath.ast;

public class NegationNode extends JmesPathNode {
  @Override
  public String toString() {
    return "NegationNode()";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof NegationNode;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
