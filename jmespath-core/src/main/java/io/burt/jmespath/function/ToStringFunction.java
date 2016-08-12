package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToStringFunction extends BaseFunction {
  public ToStringFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T subject = arguments.get(0).value();
    if (runtime.typeOf(subject) == JmesPathType.STRING) {
      return subject;
    } else {
      return runtime.createString(runtime.toString(subject));
    }
  }
}
