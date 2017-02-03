package io.burt.jmespath.function;

import io.burt.jmespath.Adapter;

/**
 * Helper base class for higher order comparison functions like max_by and min_by.
 */
public abstract class CompareByFunction extends ComparingFunction {
  /**
   * Subclasses override this method to decide whether the greatest or least
   * element sorts first.
   */
  protected abstract boolean sortsBefore(int compareResult);

  @Override
  protected <T> ComparingFunction.Aggregator<T> createAggregator(Adapter<T> runtime, T element, T elementValue) {
    return new ComparingAggregator<T>(runtime, element, elementValue);
  }

  @Override
  protected <T> T createNullValue(Adapter<T> runtime) {
    return runtime.createNull();
  }

  private class ComparingAggregator<V> extends ComparingFunction.Aggregator<V> {
    private Pair<V> current;

    public ComparingAggregator(Adapter<V> runtime, V initialElement, V initialValue) {
      super(runtime);
      this.current = new Pair<V>(initialValue, initialElement);
    }

    protected void aggregate(V candidate, V candidateValue) {
      if (sortsBefore(runtime.compare(candidateValue, current.elementValue))) {
        current = new Pair<V>(candidateValue, candidate);
      }
    }
  
    protected V result() {
      return current.element;
    }
  }

  private static class Pair<U> {
    public final U elementValue;
    public final U element;

    public Pair(U elementValue, U element) {
      this.elementValue = elementValue;
      this.element = element;
    }
  }
}
