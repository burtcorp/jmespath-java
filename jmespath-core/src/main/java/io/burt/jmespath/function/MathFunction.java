package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

/**
 * Helper base class for functions that perform operations on a single numerical
 * argument, like calculating the absolute value, rounding, etc.
 */
public abstract class MathFunction extends BaseFunction {
  public MathFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T value = arguments.get(0).value();
    double n = runtime.toNumber(value).doubleValue();
    return runtime.createNumber(performMathOperation(n));
  }

  /**
   * Subclasses implement this method.
   */
  protected abstract double performMathOperation(double n);
}
