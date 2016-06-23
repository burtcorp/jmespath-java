package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class TypeFunction extends JmesPathFunction {
  public TypeFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "any value", "expression");
    } else {
      return adapter.createString(adapter.typeOf(argument.value()).toString());
    }
  }
}
