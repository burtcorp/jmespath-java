package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

@Function(arity = 1)
public abstract class ArrayMathFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), expectedType(), "expression");
    } else {
      T array = argument.value();
      if (isValidArray(adapter, array)) {
        List<T> values = adapter.toList(array);
        return performMathOperation(adapter, values);
      } else {
        List<T> values = adapter.toList(array);
        List<String> types = new ArrayList<>(values.size());
        for (T value : values) {
          types.add(adapter.typeOf(value).toString());
        }
        throw new ArgumentTypeException(name(), expectedType(), types.toString());
      }
    }
  }

  protected abstract <T> T performMathOperation(Adapter<T> adapter, List<T> values);

  protected abstract String expectedType();

  protected abstract <T> boolean isValidArray(Adapter<T> adapter, T array);
}
