package io.burt.jmespath.ast;

import java.util.Arrays;

public class MultiSelectListNode extends JmesPathNode {
  private final JmesPathNode[] elements;

  public MultiSelectListNode(JmesPathNode... elements) {
    this.elements = elements;
  }

  protected JmesPathNode[] elements() {
    return elements;
  }

  @Override
  public String toString() {
    StringBuilder elementsString = new StringBuilder();
    for (JmesPathNode element : elements) {
      elementsString.append(", ").append(element);
    }
    elementsString.delete(0, 2);
    return String.format("MultiSelectListNode([%s])", elementsString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MultiSelectListNode)) {
      return false;
    }
    MultiSelectListNode other = (MultiSelectListNode) o;
    return Arrays.equals(this.elements(), other.elements());
  }

  @Override
  public int hashCode() {
    int h = 1;
    for (JmesPathNode element : elements) {
      h = h * 31 + element.hashCode();
    }
    return h;
  }
}
