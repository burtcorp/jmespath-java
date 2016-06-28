package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

@Function(arity = 1)
public class SortFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "array of numbers or strings", "expression");
    } else {
      T array = argument.value();
      if (TypesHelper.isNumberArray(adapter, array) || TypesHelper.isStringArray(adapter, array)) {
        List<T> elements = new ArrayList(adapter.toList(array));
        Collections.sort(elements, adapter);
        return adapter.createArray(elements);
      } else {
        throw new ArgumentTypeException(name(), "array of numbers or strings", adapter.typeOf(array).toString());
      }
    }
  }
}
