package io.burt.jmespath;

/**
 * A runtime keeps track of everything that is necessary to compile and execute
 * JMESPath expressions.
 */
public interface JmesPathRuntime<T> {
  /**
   * Compile a JMESPath expression into a reusable expression object.
   *
   * The expression objects should be stateless and thread safe.
   *
   * @throw ParseException when the string is not a valid JMESPath expression
   */
  public JmesPathExpression<T> compile(String expression);
}
