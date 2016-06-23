package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class ReverseFunction extends JmesPathFunction {
  public ReverseFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "array or string", "expression");
    } else {
      T subject = argument.value();
      if (adapter.isArray(subject)) {
        List<T> elements = new ArrayList(adapter.toList(subject));
        Collections.reverse(elements);
        return adapter.createArray(elements);
      } else if (adapter.isString(subject)) {
        return adapter.createString(new StringBuilder(adapter.toString(subject)).reverse().toString());
      } else {
        throw new ArgumentTypeException(name(), "array or string", adapter.typeOf(subject).toString());
      }
    }
  }
}
