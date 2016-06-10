package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class ToNumberFunction extends JmesPathFunction {
  public ToNumberFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T argument = arguments.get(0).value();
    if (adapter.isNumber(argument)) {
      return argument;
    } else if (adapter.isString(argument)) {
      try {
        return adapter.createNumber(Double.parseDouble(adapter.toString(argument)));
      } catch (NumberFormatException nfe) {
        return adapter.createNull();
      }
    } else {
      return adapter.createNull();
    }
  }
}
