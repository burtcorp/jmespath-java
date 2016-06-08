package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class EndsWithFunction extends JmesPathFunction {
  public EndsWithFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    T suffix = arguments.get(1).value();
    if (adapter.isString(subject) && adapter.isString(suffix)) {
      return adapter.createBoolean(adapter.toString(subject).endsWith(adapter.toString(suffix)));
    } else {
      throw new FunctionCallException(String.format("Expected two strings got %s and %s", adapter.typeOf(subject), adapter.typeOf(suffix)));
    }
  }
}