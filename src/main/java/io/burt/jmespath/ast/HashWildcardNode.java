package io.burt.jmespath.ast;

public class HashWildcardNode extends JmesPathNode {
  @Override
  public String toString() {
    return "HashWildcardNode()";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof HashWildcardNode;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
