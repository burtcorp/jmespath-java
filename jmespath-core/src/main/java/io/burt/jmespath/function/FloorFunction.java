package io.burt.jmespath.function;

public class FloorFunction extends MathFunction {
  @Override
  protected Double performMathOperation(Double d) {
    return Math.floor(d);
  }
}
