package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.JmesPathRuntime;

public abstract class ArrayMathFunction extends JmesPathFunction {
  public ArrayMathFunction(ArgumentConstraint innerConstraint) {
    super(ArgumentConstraints.arrayOf(innerConstraint));
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    return performMathOperation(runtime, runtime.toList(arguments.get(0).value()));
  }

  protected abstract <T> T performMathOperation(JmesPathRuntime<T> runtime, List<T> values);
}
