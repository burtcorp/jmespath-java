package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import io.burt.jmespath.Adapter;

public class SortByFunction extends TransformByFunction {
  @Override
  protected <T> TransformByFunction.Aggregator<T> createAggregator(Adapter<T> runtime, int elementCount, T element, T elementValue) {
    return new SortingAggregator<T>(runtime, elementCount, element, elementValue);
  }

  @Override
  protected <T> T createNullValue(Adapter<T> runtime) {
    return runtime.createArray(new ArrayList<T>());
  }

  private class SortingAggregator<V> extends TransformByFunction.Aggregator<V> {
    private List<Pair<V>> pairs;

    public SortingAggregator(Adapter<V> runtime, int elementCount, V initialElement, V initialValue) {
      super(runtime);
      this.pairs = new ArrayList<>(elementCount);
      this.pairs.add(new Pair<V>(initialElement, initialValue));
    }

    protected void aggregate(V candidate, V candidateValue) {
      pairs.add(new Pair<V>(candidate, candidateValue));
    }
  
    protected V result() {
      return runtime.createArray(sortAndFlatten(runtime, pairs));
    }

    private <T> List<T> sortAndFlatten(final Adapter<T> runtime, List<Pair<T>> pairs) {
      Collections.sort(pairs, new Comparator<Pair<T>>() {
        @Override
        public int compare(Pair<T> a, Pair<T> b) {
          return runtime.compare(a.elementValue, b.elementValue);
        }
      });
      List<T> sorted = new ArrayList<>(pairs.size());
      for (Pair<T> pair : pairs) {
        sorted.add(pair.element);
      }
      return sorted;
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
