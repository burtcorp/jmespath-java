package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class SumFunction extends ArrayMathFunction {
  public SumFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T performMathOperation(Adapter<T> adapter, List<T> values) {
    double sum = 0;
    for (T n : values) {
      sum += adapter.toNumber(n).doubleValue();
    }
    return adapter.createNumber(sum);
  }
}
