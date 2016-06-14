package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class NotNullFunction extends JmesPathFunction {
  public NotNullFunction() {
    super(1, Integer.MAX_VALUE);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (argument.isExpression()) {
        throw new ArgumentTypeException(name(), "any value", "expression");
      } else {
        if (!adapter.isNull(argument.value())) {
          return argument.value();
        }
      }
    }
    return adapter.createNull();
  }
}
