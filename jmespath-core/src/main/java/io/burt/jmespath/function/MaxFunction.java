package io.burt.jmespath.function;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public class MaxFunction extends ArrayMathFunction {
  @Override
  protected <T> Number performMathOperation(Adapter<T> adapter, List<T> values) {
    Iterator<T> vs = values.iterator();
    double max = adapter.toDouble(vs.next());
    while (vs.hasNext()) {
      double v = adapter.toDouble(vs.next());
      if (v > max) {
        max = v;
      }
    }
    return max;
  }
}
