package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.ast.JmesPathNode;

public class MapFunction extends JmesPathFunction {
  @Override
  public <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    JmesPathNode expression = arguments.get(0).expression();
    List<T> array = adapter.toList(arguments.get(1).value());
    List<T> result = new ArrayList<>(array.size());
    for (T element : array) {
      result.add(expression.evaluate(adapter, element));
    }
    return adapter.createArray(result);
  }
}
