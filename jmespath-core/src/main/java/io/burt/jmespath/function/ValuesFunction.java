package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class ValuesFunction extends JmesPathFunction {
  public ValuesFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "object", "expression");
    } else {
      T subject = argument.value();
      if (adapter.isObject(subject)) {
        return adapter.createArray(adapter.toList(subject));
      } else {
        throw new ArgumentTypeException(name(), "object", adapter.typeOf(subject));
      }
    }
  }
}
