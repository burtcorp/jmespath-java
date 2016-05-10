package io.burt.jmespath.ast;

public class CurrentNodeNode extends JmesPathNode {
  @Override
  public String toString() {
    return "CurrentNodeNode()";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof CurrentNodeNode;
  }

  @Override
  public int hashCode() {
    return 31;
  }
}
