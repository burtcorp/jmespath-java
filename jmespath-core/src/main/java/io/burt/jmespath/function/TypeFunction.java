package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;

public class TypeFunction extends JmesPathFunction {
  public TypeFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    return runtime.createString(runtime.typeOf(arguments.get(0).value()).toString());
  }
}
