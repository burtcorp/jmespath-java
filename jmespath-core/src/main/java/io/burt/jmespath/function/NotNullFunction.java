package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class NotNullFunction extends JmesPathFunction {
  public NotNullFunction() {
    super(ArgumentConstraints.listOf(1, Integer.MAX_VALUE, ArgumentConstraints.anyValue()));
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (adapter.typeOf(argument.value()) != JmesPathType.NULL) {
        return argument.value();
      }
    }
    return adapter.createNull();
  }
}
