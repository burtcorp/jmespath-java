package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class StartsWithFunction extends BaseFunction {
  public StartsWithFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.typeOf(JmesPathType.STRING)
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    T prefix = arguments.get(1).value();
    return runtime.createBoolean(runtime.toString(subject).startsWith(runtime.toString(prefix)));
  }
}
