package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class UpperCaseFunction extends BaseFunction {
  public UpperCaseFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.STRING));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T arg = arguments.get(0).value();
    return runtime.createString(runtime.toString(arg).toUpperCase());
  }
}
