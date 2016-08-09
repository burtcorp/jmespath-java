package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class FlattenArrayNode extends JmesPathNode {
  public FlattenArrayNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> runtime, T currentValue) {
    if (!isProjection() && runtime.typeOf(currentValue) != JmesPathType.ARRAY) {
      return runtime.createNull();
    } else {
      List<T> elements = runtime.toList(currentValue);
      List<T> flattened = new LinkedList<>();
      for (T element : elements) {
        if (runtime.typeOf(element) == JmesPathType.ARRAY) {
          flattened.addAll(runtime.toList(element));
        } else {
          flattened.add(element);
        }
      }
      return runtime.createArray(flattened);
    }
  }

  @Override
  protected boolean internalEquals(Object o) {
    return true;
  }

  @Override
  protected int internalHashCode() {
    return 19;
  }
}
