package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public abstract class MathFunction extends JmesPathFunction {
  public MathFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T value = arguments.get(0).value();
    double n = adapter.toNumber(value).doubleValue();
    return adapter.createNumber(performMathOperation(n));
  }

  protected abstract double performMathOperation(double n);
}
