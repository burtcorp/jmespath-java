package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.JmesPathNode;

@Function(arity = 2)
public class MapFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    JmesPathNode expression = arguments.get(0).expression();
    T array = arguments.get(1).value();
    if (arguments.get(0).isValue()) {
      throw new ArgumentTypeException(name(), "expression", adapter.typeOf(arguments.get(0).value()).toString());
    }
    if (arguments.get(1).isExpression()) {
      throw new ArgumentTypeException(name(), "array of objects", "expression");
    } else if (adapter.typeOf(array) != JmesPathType.ARRAY) {
      throw new ArgumentTypeException(name(), "array of objects", adapter.typeOf(array).toString());
    }
    List<T> elements = adapter.toList(array);
    List<T> result = new ArrayList<>(elements.size());
    for (T element : elements) {
      result.add(expression.evaluate(adapter, element));
    }
    return adapter.createArray(result);
  }
}
