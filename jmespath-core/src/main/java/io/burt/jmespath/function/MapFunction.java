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
    T array = arguments.get(1).value();
    if (arguments.get(0).isValue()) {
      throw new ArgumentTypeException(name(), "expression", adapter.typeOf(arguments.get(0).value()));
    }
    if (arguments.get(1).isExpression()) {
      throw new ArgumentTypeException(name(), "array of objects", "expression");
    } else if (!adapter.isArray(array)) {
      throw new ArgumentTypeException(name(), "array of objects", adapter.typeOf(array));
    }
    List<T> elements = adapter.toList(array);
    List<T> result = new ArrayList<>(elements.size());
    for (T element : elements) {
      result.add(expression.evaluate(adapter, element));
    }
    return adapter.createArray(result);
  }
}
