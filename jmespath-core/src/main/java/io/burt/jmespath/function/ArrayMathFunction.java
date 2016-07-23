package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public abstract class ArrayMathFunction extends JmesPathFunction {
  public ArrayMathFunction(ArgumentConstraint innerConstraint) {
    super(ArgumentConstraints.arrayOf(innerConstraint));
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    return performMathOperation(adapter, adapter.toList(arguments.get(0).value()));
  }

  protected abstract <T> T performMathOperation(Adapter<T> adapter, List<T> values);
}
