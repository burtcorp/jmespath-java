package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class SumFunction extends ArrayMathFunction {
  @Override
  protected <T> boolean isValidArray(Adapter<T> adapter, T array) {
    return TypesHelper.isNumberArray(adapter, array);
  }

  @Override
  protected String expectedType() {
    return "array of numbers";
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    double sum = 0;
    for (T n : values) {
      sum += adapter.toDouble(n);
    }
    return adapter.createNumber(sum);
  }
}
