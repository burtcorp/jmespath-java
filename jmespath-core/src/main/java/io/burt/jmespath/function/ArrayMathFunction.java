package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public abstract class ArrayMathFunction extends BaseFunction {
  public ArrayMathFunction(ArgumentConstraint innerConstraint) {
    super(ArgumentConstraints.arrayOf(innerConstraint));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return performMathOperation(runtime, runtime.toList(arguments.get(0).value()));
  }

  protected abstract <T> T performMathOperation(Adapter<T> runtime, List<T> values);
}
