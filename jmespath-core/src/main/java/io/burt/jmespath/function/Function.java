package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

/**
 * Represents the implementation of a function available in JMESPath expressions.
 */
public interface Function {
  /**
   * Returns the name of the function.
   * <p>
   * The name is either automatically generated from the class name, or
   * explicitly specified in the constructor.
   */
  public String name();

  /**
   * Call this function with a list of arguments.
   *
   * The arguments can be either values or expressions, and will be checked
   * by the before the function runs.
   */
  public <T> T call(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments);
}
