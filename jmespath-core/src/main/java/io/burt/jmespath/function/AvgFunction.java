package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class AvgFunction extends ArrayMathFunction {
  public AvgFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> runtime, List<T> values) {
    if (values.isEmpty()) {
      return runtime.createNull();
    } else {
      double sum = 0;
      int count = 0;
      for (T n : values) {
        sum += runtime.toNumber(n).doubleValue();
        count += 1;
      }
      return runtime.createNumber(sum/count);
    }
  }
}
