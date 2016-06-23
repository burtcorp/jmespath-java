package io.burt.jmespath.function;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class KeysFunction extends JmesPathFunction {
  public KeysFunction() {
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
        return adapter.createArray(adapter.getPropertyNames(subject));
      } else {
        throw new ArgumentTypeException(name(), "object", adapter.typeOf(subject).toString());
      }
    }
  }
}
