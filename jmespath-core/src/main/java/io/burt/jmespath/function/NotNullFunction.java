package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(minArity = 1, maxArity = Integer.MAX_VALUE)
public class NotNullFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (argument.isExpression()) {
        throw new ArgumentTypeException(name(), "any value", "expression");
      } else {
        if (adapter.typeOf(argument.value()) != JmesPathType.NULL) {
          return argument.value();
        }
      }
    }
    return adapter.createNull();
  }
}
