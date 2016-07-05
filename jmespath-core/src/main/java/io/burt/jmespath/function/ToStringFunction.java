package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToStringFunction extends JmesPathFunction {
  public ToStringFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (adapter.typeOf(subject) == JmesPathType.STRING) {
      return subject;
    } else {
      return adapter.createString(adapter.toString(subject));
    }
  }
}
