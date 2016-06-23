package io.burt.jmespath.function;

public class AbsFunction extends MathFunction {
  @Override
  protected double performMathOperation(double n) {
    return Math.abs(n);
  }
}
