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
      if (values.isEmpty()) {
        return adapter.createNull();
      } else {
        return performMathOperation(adapter, values);
      }
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

  protected <T> boolean isNumberArray(Adapter<T> adapter, T array) {
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

  protected <T> boolean isStringArray(Adapter<T> adapter, T array) {
    if (adapter.isArray(array)) {
      for (T element : adapter.toList(array)) {
        if (!adapter.isString(element)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
