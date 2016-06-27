package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class NotNullFunction extends JmesPathFunction {
  public NotNullFunction() {
    super(1, Integer.MAX_VALUE);
  }

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
