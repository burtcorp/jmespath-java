package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class NormalizeSpaceFunction extends BaseFunction {
  public NormalizeSpaceFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T arg = arguments.get(0).value();
    return runtime.createString(runtime.toString(arg).replaceAll("\\s+", " ").trim());
  }
}
