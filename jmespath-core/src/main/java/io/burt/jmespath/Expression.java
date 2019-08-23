package io.burt.jmespath;

/**
 * A compiled JMESPath expression that can be used to search a JSON-like structure.
 * <p>
 * Expression objects should be stateless and thread safe, but the exact details
 * are up to the concrete implementations.
 */
public interface Expression<T> {
  /**
   * Evaluate this expression against a JSON-like structure and return the result.
   */
  T search(T input);
}
