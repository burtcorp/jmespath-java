package io.burt.jmespath.ast;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;

abstract class ProjectionNode extends JmesPathNode {
  public ProjectionNode(JmesPathNode source) {
    super(source);
  }

  @Override
  protected <T> T evaluateWithCurrentValue(Adapter<T> adapter, T currentValue) {
    if (isProjection()) {
      List<T> outputs = new LinkedList<>();
      for (T projectionElement : adapter.toList(currentValue)) {
        outputs.add(evaluateOne(adapter, projectionElement));
      }
      return adapter.createArray(outputs, true);
    } else {
      return evaluateOne(adapter, currentValue);
    }
  }

  protected abstract <T> T evaluateOne(Adapter<T> adapter, T projectionElement);
}
