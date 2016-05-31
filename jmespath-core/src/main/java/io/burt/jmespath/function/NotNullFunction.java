package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class NotNullFunction extends JmesPathFunction {
  public NotNullFunction() {
    super(1, Integer.MAX_VALUE);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> x : arguments) {
      if (!adapter.isNull(x.value())) {
        return x.value();
      }
    }
    return adapter.createNull();
  }
}
