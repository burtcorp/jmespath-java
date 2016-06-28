package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 2)
public class ContainsFunction extends JmesPathFunction {
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
      JmesPathType haystackType = adapter.typeOf(haystack);
      if (haystackType == JmesPathType.ARRAY) {
        return adapter.createBoolean(adapter.toList(haystack).contains(needle));
      } else if (haystackType == JmesPathType.STRING) {
        return adapter.createBoolean(adapter.toString(haystack).indexOf(adapter.toString(needle)) >= 0);
      } else {
        throw new ArgumentTypeException(name(), "array", haystackType.toString());
      }
    }
  }
}
