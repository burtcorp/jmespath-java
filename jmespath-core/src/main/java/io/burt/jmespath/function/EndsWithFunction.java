package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class EndsWithFunction extends JmesPathFunction {
  public EndsWithFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression() || secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "string", "expression");
    } else {
      T subject = firstArgument.value();
      T suffix = secondArgument.value();
      if (adapter.isString(subject) && adapter.isString(suffix)) {
        return adapter.createBoolean(adapter.toString(subject).endsWith(adapter.toString(suffix)));
      } else if (!adapter.isString(subject)) {
        throw new ArgumentTypeException(name(), "string", adapter.typeOf(subject));
      } else {
        throw new ArgumentTypeException(name(), "string", adapter.typeOf(suffix));
      }
    }
  }
}
