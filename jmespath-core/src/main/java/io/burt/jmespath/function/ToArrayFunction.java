package io.burt.jmespath.function;

import java.util.List;
import java.util.Arrays;

import io.burt.jmespath.Adapter;

public class ToArrayFunction extends JmesPathFunction {
  public ToArrayFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T argument = arguments.get(0).value();
    if (adapter.isArray(argument)) {
      return argument;
    } else {
      return adapter.createArray(Arrays.asList(argument));
    }
  }
}
