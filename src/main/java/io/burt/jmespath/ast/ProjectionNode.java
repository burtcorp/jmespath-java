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
  public <T> T evaluate(Adapter<T> adapter, T currentValue) {
    T input = source().evaluate(adapter, currentValue);
    if (isProjection()) {
      List<T> inputs = adapter.toList(input);
      List<T> outputs = new LinkedList<>();
      for (T element : inputs) {
        outputs.add(evaluateOne(adapter, element));
      }
      return adapter.combine(outputs);
    } else {
      return evaluateOne(adapter, input);
    }
  }

  protected abstract <T> T evaluateOne(Adapter<T> adapter, T currentValue);
}
