package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

@Function(arity = 1)
public class TypeFunction extends JmesPathFunction {
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
