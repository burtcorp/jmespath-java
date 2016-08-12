package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

/**
 * Helper base class for functions that take an array and return a single value,
 * like calculating the max, min or sum.
 * <p>
 * Subclasses can operate on any type of elements, and must provide that type
 * as argument when calling <code>super</code>.
 */
public abstract class ArrayMathFunction extends BaseFunction {
  public ArrayMathFunction(ArgumentConstraint innerConstraint) {
    super(ArgumentConstraints.arrayOf(innerConstraint));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return performMathOperation(runtime, runtime.toList(arguments.get(0).value()));
  }

  /**
   * Subclasses implement this method.
   */
  protected abstract <T> T performMathOperation(Adapter<T> runtime, List<T> values);
}
