package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class AvgFunction extends JmesPathFunction {
  public AvgFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    if (isNumberArray(adapter, array)) {
      double sum = 0;
      int count = 0;
      for (T n : adapter.toList(array)) {
        sum += adapter.toDouble(n);
        count += 1;
      }
      return adapter.createNumber(sum/count);
    } else {
      throw new FunctionCallException(String.format("Expected array of numbers"));
    }
  }

  private <T> boolean isNumberArray(Adapter<T> adapter, T array) {
    if (adapter.typeOf(array).equals("array")) {
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
}
