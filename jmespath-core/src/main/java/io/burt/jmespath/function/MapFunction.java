package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class MapFunction extends JmesPathFunction {
  public MapFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    JmesPathNode expression = arguments.get(0).expression();
    List<T> array = adapter.toList(arguments.get(1).value());
    List<T> result = new ArrayList<>(array.size());
    for (T element : array) {
      result.add(expression.evaluate(adapter, element));
    }
    return adapter.createArray(result);
  }
}
