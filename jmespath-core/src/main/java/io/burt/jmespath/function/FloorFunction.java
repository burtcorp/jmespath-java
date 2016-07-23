package io.burt.jmespath.function;

public class FloorFunction extends MathFunction {
  @Override
  protected double performMathOperation(double n) {
    return Math.floor(n);
  }
}
