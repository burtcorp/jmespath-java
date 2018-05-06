package io.burt.jmespath.function;

import io.burt.jmespath.Adapter;

/**
 * Helper base class for higher order comparison functions like max_by and min_by.
 */
public abstract class CompareByFunction extends TransformByFunction {
  /**
   * Subclasses override this method to decide whether the greatest or least
   * element sorts first.
   */
  protected abstract boolean sortsBefore(int compareResult);

  @Override
  protected <T> TransformByFunction.Aggregator<T> createAggregator(Adapter<T> runtime, int elementCount, T element, T elementValue) {
    return new ComparingAggregator<T>(runtime, element, elementValue);
  }

  @Override
  protected <T> T createNullValue(Adapter<T> runtime) {
    return runtime.createNull();
  }

  private class ComparingAggregator<V> extends TransformByFunction.Aggregator<V> {
    private Pair<V> current;

    public ComparingAggregator(Adapter<V> runtime, V initialElement, V initialValue) {
      super(runtime);
      this.current = new Pair<V>(initialElement, initialValue);
    }

    protected void aggregate(V candidate, V candidateValue) {
      if (sortsBefore(runtime.compare(candidateValue, current.elementValue))) {
        current = new Pair<V>(candidate, candidateValue);
      }
    }
  
    protected V result() {
      return current.element;
    }
  }

  private static class Pair<U> {
    public final U element;
    public final U elementValue;

    public Pair(U element, U elementValue) {
      this.element = element;
      this.elementValue = elementValue;
    }
  }
}
