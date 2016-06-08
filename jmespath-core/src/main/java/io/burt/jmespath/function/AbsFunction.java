package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class AbsFunction extends JmesPathFunction {
  public AbsFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T value = arguments.get(0).value();
    String argumentType = adapter.typeOf(value);
    if (argumentType.equals("number")) {
      Double d = adapter.toDouble(value);
      return adapter.createNumber(Math.abs(d));
    } else {
      throw new FunctionCallException(String.format("Expected number got %s", adapter.typeOf(value)));
    }
  }
}
