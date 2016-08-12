package io.burt.jmespath;

/**
 * A JMESPath runtime can compile JMESPath expressions.
 */
public interface JmesPath<T> {
  /**
   * Compile a JMESPath expression into a reusable expression object.
   * <p>
   * The expression objects should be stateless and thread safe, but the exact
   * details are up to the concrete implementations.
   *
   * @throws ParseException when the string is not a valid JMESPath expression
   */
  public Expression<T> compile(String expression);
}
