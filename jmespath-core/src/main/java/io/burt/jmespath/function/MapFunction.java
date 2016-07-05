package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.JmesPathNode;

public class MapFunction extends JmesPathFunction {
  public MapFunction() {
    super(
      ArgumentConstraints.listOf(
        ArgumentConstraints.expression(),
        ArgumentConstraints.arrayOf(
          ArgumentConstraints.typeOf(JmesPathType.OBJECT)
        )
      )
    );
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    JmesPathNode expression = arguments.get(0).expression();
    T array = arguments.get(1).value();
    List<T> elements = adapter.toList(array);
    List<T> result = new ArrayList<>(elements.size());
    for (T element : elements) {
      result.add(expression.evaluate(adapter, element));
    }
    return adapter.createArray(result);
  }
}
