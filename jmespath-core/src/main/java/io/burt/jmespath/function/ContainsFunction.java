package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class ContainsFunction extends JmesPathFunction {
  public ContainsFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "array", "expression");
    } else if (secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "any value", "expression");
    } else {
      T haystack = firstArgument.value();
      T needle = secondArgument.value();
      if (adapter.isArray(haystack)) {
        return adapter.createBoolean(adapter.toList(haystack).contains(needle));
      } else if (adapter.isString(haystack)) {
        return adapter.createBoolean(adapter.toString(haystack).indexOf(adapter.toString(needle)) >= 0);
      } else {
        throw new ArgumentTypeException(name(), "array", adapter.typeOf(haystack));
      }
    }
  }
}
