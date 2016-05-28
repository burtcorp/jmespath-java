package io.burt.jmespath.ast;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

public class FlattenObjectNode extends JmesPathNode {
  public FlattenObjectNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    if (isProjection()) {
      List<T> flattened = new LinkedList<>();
      for (T projectionElement : adapter.toList(currentValue)) {
        flattened.add(adapter.createArray(adapter.toList(projectionElement), true));
      }
      return adapter.createArray(flattened, true);
    } else if (adapter.isObject(currentValue)) {
      return adapter.createArray(adapter.toList(currentValue), true);
    } else {
      return adapter.createNull();
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
