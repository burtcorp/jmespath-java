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
    if (isNumberArray(adapter, array)) {
      List<T> values = adapter.toList(array);
      if (values.isEmpty()) {
        return adapter.createNull();
      } else {
        Number n = performMathOperation(adapter, values);
        if (n == null) {
          return adapter.createNull();
        } else {
          return adapter.createNumber(n.doubleValue());
        }
      }
    } else {
      List<T> values = adapter.toList(array);
      List<String> types = new ArrayList<>(values.size());
      for (T value : values) {
        types.add(adapter.typeOf(value));
      }
      throw new ArgumentTypeException(name(), "array of numbers", types.toString());
    }
  }

  private <T> boolean isNumberArray(Adapter<T> adapter, T array) {
    if (adapter.isArray(array)) {
      for (T element : adapter.toList(array)) {
        if (!adapter.isNumber(element)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  protected abstract <T> Number performMathOperation(Adapter<T> adapter, List<T> values);
}
