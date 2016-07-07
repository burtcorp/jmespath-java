package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class StartsWithFunction extends JmesPathFunction {
  public StartsWithFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.typeOf(JmesPathType.STRING)
    );
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    T prefix = arguments.get(1).value();
    return adapter.createBoolean(adapter.toString(subject).startsWith(adapter.toString(prefix)));
  }
}
