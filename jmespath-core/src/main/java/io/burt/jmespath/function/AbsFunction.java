package io.burt.jmespath.function;

public class AbsFunction extends MathFunction {
  @Override
  protected Double performMathOperation(Double d) {
    return Math.abs(d);
  }
}
