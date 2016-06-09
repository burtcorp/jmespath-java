package io.burt.jmespath.function;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public class MaxFunction extends ArrayMathFunction {
  @Override
  protected <T> boolean isValidArray(Adapter<T> adapter, T array) {
    return isNumberArray(adapter, array) || isStringArray(adapter, array);
  }

  @Override
  protected String expectedType() {
    return "array of numbers or an array of strings";
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    Iterator<T> vs = values.iterator();
    T max = vs.next();
    while (vs.hasNext()) {
      T candidate = vs.next();
      if (adapter.compare(candidate, max) > 0) {
        max = candidate;
      }
    }
    return max;
  }
}
