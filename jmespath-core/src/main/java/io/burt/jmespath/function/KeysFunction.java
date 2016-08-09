package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class KeysFunction extends JmesPathFunction {
  public KeysFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.OBJECT));
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    return runtime.createArray(runtime.getPropertyNames(arguments.get(0).value()));
  }
}
