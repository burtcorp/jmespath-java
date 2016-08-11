package io.burt.jmespath;

/**
 * A compiled JMESPath expression that can be used to search a structure.
 */
public interface Expression<T> {
  /**
   * Evaluate this expression against a structure and return the result.
   */
  public T search(T input);
}
