package io.burt.jmespath.ast;

public class ListWildcardNode extends JmesPathNode {
  @Override
  public String toString() {
    return "ListWildcardNode()";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof ListWildcardNode;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
