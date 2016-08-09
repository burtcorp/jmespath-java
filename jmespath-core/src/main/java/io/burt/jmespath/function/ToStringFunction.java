package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class ToStringFunction extends JmesPathFunction {
  public ToStringFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (runtime.typeOf(subject) == JmesPathType.STRING) {
      return subject;
    } else {
      return runtime.createString(runtime.toString(subject));
    }
  }
}
