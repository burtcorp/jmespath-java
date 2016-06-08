package io.burt.jmespath.function;

public class CeilFunction extends MathFunction {
  @Override
  protected Double performMathOperation(Double d) {
    return Math.ceil(d);
  }
}