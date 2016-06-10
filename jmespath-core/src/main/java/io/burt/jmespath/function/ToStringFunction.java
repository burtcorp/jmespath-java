package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class ToStringFunction extends JmesPathFunction {
  public ToStringFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T argument = arguments.get(0).value();
    if (adapter.isString(argument)) {
      return argument;
    } else {
      return adapter.createString(adapter.toString(argument));
    }
  }
}
