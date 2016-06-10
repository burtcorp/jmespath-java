package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class StartsWithFunction extends JmesPathFunction {
  public StartsWithFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    T suffix = arguments.get(1).value();
    if (adapter.isString(subject) && adapter.isString(suffix)) {
      return adapter.createBoolean(adapter.toString(subject).startsWith(adapter.toString(suffix)));
    } else if (!adapter.isString(subject)) {
      throw new ArgumentTypeException(name(), "string", adapter.typeOf(subject));
    } else {
      throw new ArgumentTypeException(name(), "string", adapter.typeOf(suffix));
    }
  }
}
