package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class CeilFunction extends JmesPathFunction {
  public CeilFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T value = arguments.get(0).value();
    if (adapter.isNumber(value)) {
      Double d = adapter.toDouble(value);
      return adapter.createNumber(Math.ceil(d));
    } else {
      throw new FunctionCallException(String.format("Expected number got %s", adapter.typeOf(value)));
    }
  }
}
