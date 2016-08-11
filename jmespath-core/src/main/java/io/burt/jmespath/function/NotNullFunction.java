package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class NotNullFunction extends BaseFunction {
  public NotNullFunction() {
    super(ArgumentConstraints.listOf(1, Integer.MAX_VALUE, ArgumentConstraints.anyValue()));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (runtime.typeOf(argument.value()) != JmesPathType.NULL) {
        return argument.value();
      }
    }
    return runtime.createNull();
  }
}
