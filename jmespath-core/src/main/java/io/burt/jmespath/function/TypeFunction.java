package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class TypeFunction extends BaseFunction {
  public TypeFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ValueOrExpression<T>> arguments) {
    return runtime.createString(runtime.typeOf(arguments.get(0).value()).toString());
  }
}
