package io.burt.jmespath.function;

import java.util.List;
import java.util.Arrays;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 1)
public class ToArrayFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "any value", "expression");
    } else {
      T subject = argument.value();
      if (adapter.typeOf(subject) == JmesPathType.ARRAY) {
        return subject;
      } else {
        return adapter.createArray(Arrays.asList(subject));
      }
    }
  }
}
