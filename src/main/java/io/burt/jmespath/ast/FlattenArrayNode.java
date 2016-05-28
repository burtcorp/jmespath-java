package io.burt.jmespath.ast;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

public class FlattenArrayNode extends JmesPathNode {
  public FlattenArrayNode(JmesPathNode source) {
    super(source);
  }

  @Override
  public <T> T evaluate(Adapter<T> adapter, T input) {
    T currentValue = source().evaluate(adapter, input);
    if (!source().isProjection() && !adapter.isArray(currentValue)) {
      return adapter.createNull();
    } else {
      List<T> elements = adapter.toList(currentValue);
      List<T> flattened = new LinkedList<>();
      for (T element : elements) {
        if (adapter.isArray(element)) {
          flattened.addAll(adapter.toList(element));
        } else {
          flattened.add(element);
        }
      }
      return adapter.createArray(flattened);
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
