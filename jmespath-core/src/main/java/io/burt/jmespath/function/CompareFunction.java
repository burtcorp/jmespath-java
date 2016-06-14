package io.burt.jmespath.function;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public abstract class CompareFunction extends ArrayMathFunction {
  protected abstract boolean sortsBefore(int compareResult);

  @Override
  protected <T> boolean isValidArray(Adapter<T> adapter, T array) {
    return TypesHelper.isNumberArray(adapter, array) || TypesHelper.isStringArray(adapter, array);
  }

  @Override
  protected String expectedType() {
    return "array of numbers or an array of strings";
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    if (values.isEmpty()) {
      return adapter.createNull();
    } else {
      Iterator<T> vs = values.iterator();
      T result = vs.next();
      while (vs.hasNext()) {
        T candidate = vs.next();
        if (sortsBefore(adapter.compare(candidate, result))) {
          result = candidate;
        }
      }
      return result;
    }
  }
}
