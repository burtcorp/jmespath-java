package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ValuesFunction extends BaseFunction {
  public ValuesFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.OBJECT));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return runtime.createArray(runtime.toList(arguments.get(0).value()));
  }
}
