package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public abstract class MathFunction extends BaseFunction {
  public MathFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T value = arguments.get(0).value();
    double n = runtime.toNumber(value).doubleValue();
    return runtime.createNumber(performMathOperation(n));
  }

  protected abstract double performMathOperation(double n);
}
