package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public abstract class MathFunction extends JmesPathFunction {
  public MathFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER));
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "number", "expression");
    } else {
      T value = argument.value();
      JmesPathType type = adapter.typeOf(value);
      if (type == JmesPathType.NUMBER) {
        double n = adapter.toNumber(value).doubleValue();
        return adapter.createNumber(performMathOperation(n));
      } else {
        throw new ArgumentTypeException(name(), "number", type.toString());
      }
    }
  }

  protected abstract double performMathOperation(double n);
}
