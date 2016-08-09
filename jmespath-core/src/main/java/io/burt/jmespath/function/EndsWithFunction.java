package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class EndsWithFunction extends JmesPathFunction {
  public EndsWithFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.typeOf(JmesPathType.STRING)
    );
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    T suffix = arguments.get(1).value();
    return runtime.createBoolean(runtime.toString(subject).endsWith(runtime.toString(suffix)));
  }
}
