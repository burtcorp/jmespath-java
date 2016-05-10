package io.burt.jmespath.ast;

public class FlattenNode extends JmesPathNode {
  @Override
  public String toString() {
    return "FlattenNode()";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof FlattenNode;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
