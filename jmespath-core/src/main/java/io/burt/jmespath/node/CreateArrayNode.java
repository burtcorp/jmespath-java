package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class CreateArrayNode extends JmesPathNode {
  private final JmesPathNode[] entries;

  public CreateArrayNode(JmesPathNode[] entries, JmesPathNode source) {
    super(source);
    this.entries = entries;
  }

  protected JmesPathNode[] entries() {
    return entries;
  }

  @Override
  protected <T> T evaluateOne(JmesPathRuntime<T> runtime, T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      List<T> array = new ArrayList<>();
      for (JmesPathNode entry : entries) {
        array.add(entry.evaluate(runtime, currentValue));
      }
      return runtime.createArray(array);
    }
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("[");
    for (JmesPathNode entry : entries) {
      str.append(entry).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateArrayNode other = (CreateArrayNode) o;
    return Arrays.equals(entries(), other.entries());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (JmesPathNode node : entries) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
