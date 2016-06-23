package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public abstract class MathFunction extends JmesPathFunction {
  public MathFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "number", "expression");
    } else {
      T value = argument.value();
      if (adapter.isNumber(value)) {
        double n = adapter.toNumber(value).doubleValue();
        return adapter.createNumber(performMathOperation(n));
      } else {
        throw new ArgumentTypeException(name(), "number", adapter.typeOf(value).toString());
      }
    }
  }

  protected abstract double performMathOperation(double n);
}
