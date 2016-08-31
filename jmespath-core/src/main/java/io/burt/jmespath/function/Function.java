package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

/**
 * Represents the implementation of a function available in JMESPath expressions.
 *
 * Custom function implementations should extend {@link BaseFunction} to get
 * argument type checking, etc.
 */
public interface Function {
  /**
   * Returns the name of the function.
   * <p>
   * The name is either automatically generated from the class name, or
   * explicitly specified in the constructor.
   */
  String name();

  /**
   * Returns the constraints to use when checking the list of arguments before
   * the function is called.
   */
  ArgumentConstraint argumentConstraints();

  /**
   * Call this function with a list of arguments.
   * <p>
   * The arguments can be either values or expressions, and their types will be
   * checked before proceeding with the call.
   *
   * @throws ArgumentTypeException when the function is called with arguments of the wrong type
   * @throws ArityException when the function is called with the wrong number of arguments
   */
  <T> T call(Adapter<T> runtime, List<FunctionArgument<T>> arguments);
}
