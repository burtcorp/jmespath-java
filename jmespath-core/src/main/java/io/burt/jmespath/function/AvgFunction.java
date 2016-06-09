package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class AvgFunction extends ArrayMathFunction {
  @Override
  protected <T> boolean isValidArray(Adapter<T> adapter, T array) {
    return isNumberArray(adapter, array);
  }

  @Override
  protected String expectedType() {
    return "array of numbers";
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    double sum = 0;
    int count = 0;
    for (T n : values) {
      sum += adapter.toDouble(n);
      count += 1;
    }
    return adapter.createNumber(sum/count);
  }
}
