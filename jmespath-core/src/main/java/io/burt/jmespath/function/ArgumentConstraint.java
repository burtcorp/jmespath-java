package io.burt.jmespath.function;

import java.util.Iterator;

import io.burt.jmespath.Adapter;

/**
 * A description of the expected type of an argument or list of arguments passed
 * to a function.
 */
public interface ArgumentConstraint {
  /**
   * Check that the argument list complies with the constraints.
   * <p>
   * Most constraints will consume one or more elements from the iterator, but
   * constraints that represents optional arguments may choose not to.
   * <p>
   * Any errors found will be returned as an iterator of {@link ArgumentError}.
   * When this iterator is empty no errors were found. The iterator may or may
   * not contain all errors that could be found, errors may make the checker
   * return as soon as they are encountered and not attempt to check the
   * remaining arguments.
   * <p>
   * When <code>expectNoRemainingArguments</code> is true and there remain
   * elements in the iterator after all checks have been performed an error will
   * be returned.
   */
  <T> Iterator<ArgumentError> check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments);

  /**
   * @return the minimum number of arguments required.
   */
  int minArity();

  /**
   * @return the maximum number of arguments accepted.
   */
  int maxArity();

  /**
   * @return a string representation of the types accepted. Used to construct
   *   user friendly error messages.
   */
  String expectedType();
}
