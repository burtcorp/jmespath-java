package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class LengthFunction extends JmesPathFunction {
  public LengthFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (adapter.isString(subject)) {
      return adapter.createNumber(adapter.toString(subject).length());
    } else if (adapter.isArray(subject) || adapter.isObject(subject)) {
      return adapter.createNumber(adapter.toList(subject).size());
    } else {
      throw new ArgumentTypeException(name(), "string, array or object", adapter.typeOf(subject));
    }
  }
}
