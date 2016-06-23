package io.burt.jmespath.function;

public class CeilFunction extends MathFunction {
  @Override
  protected double performMathOperation(double n) {
    return Math.ceil(n);
  }
}
