package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public abstract class ArrayMathFunction extends JmesPathFunction {
  public ArrayMathFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    if (isValidArray(adapter, array)) {
      List<T> values = adapter.toList(array);
      return performMathOperation(adapter, values);
    } else {
      List<T> values = adapter.toList(array);
      List<String> types = new ArrayList<>(values.size());
      for (T value : values) {
        types.add(adapter.typeOf(value));
      }
      throw new ArgumentTypeException(name(), expectedType(), types.toString());
    }
  }

  protected abstract <T> T performMathOperation(Adapter<T> adapter, List<T> values);

  protected abstract String expectedType();

  protected abstract <T> boolean isValidArray(Adapter<T> adapter, T array);
}
