package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class EndsWithFunction extends BaseFunction {
  public EndsWithFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.typeOf(JmesPathType.STRING)
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T subject = arguments.get(0).value();
    T suffix = arguments.get(1).value();
    return runtime.createBoolean(runtime.toString(subject).endsWith(runtime.toString(suffix)));
  }
}
