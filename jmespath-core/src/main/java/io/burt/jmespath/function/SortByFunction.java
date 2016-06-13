package io.burt.jmespath.function;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class SortByFunction extends JmesPathFunction {
  public SortByFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(final Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    final JmesPathNode expression = arguments.get(1).expression();
    if (arguments.get(0).isExpression()) {
      throw new ArgumentTypeException(name(), "array of objects", "expression");
    } else if (!adapter.isArray(array)) {
      throw new ArgumentTypeException(name(), "array of objects", adapter.typeOf(array));
    }
    if (arguments.get(1).isValue()) {
      throw new ArgumentTypeException(name(), "expression", adapter.typeOf(arguments.get(1).value()));
    }
    List<T> elementsList = adapter.toList(array);
    Iterator<T> elements = elementsList.iterator();
    if (elements.hasNext()) {
      List<Pair<T>> pairs = new ArrayList<>(elementsList.size());
      T element = elements.next();
      T transformedElement = expression.evaluate(adapter, element);
      boolean expectNumbers = true;
      if (adapter.isString(transformedElement)) {
        expectNumbers = false;
      } else if (!adapter.isNumber(transformedElement)) {
        throw new ArgumentTypeException(name(), "number or string", adapter.typeOf(transformedElement));
      }
      pairs.add(new Pair(transformedElement, element));
      while (elements.hasNext()) {
        element = elements.next();
        transformedElement = expression.evaluate(adapter, element);
        if (expectNumbers && !adapter.isNumber(transformedElement)) {
          throw new ArgumentTypeException(name(), "number", adapter.typeOf(transformedElement));
        } else if (!expectNumbers && !adapter.isString(transformedElement)) {
          throw new ArgumentTypeException(name(), "string", adapter.typeOf(transformedElement));
        }
        pairs.add(new Pair(transformedElement, element));
      }
      Collections.sort(pairs, new Comparator<Pair<T>>() {
        @Override
        public int compare(Pair<T> a, Pair<T> b) {
          return adapter.compare(a.transformedElement, b.transformedElement);
        }
      });
      List<T> sorted = new ArrayList<>(pairs.size());
      for (Pair<T> pair : pairs) {
        sorted.add(pair.element);
      }
      return adapter.createArray(sorted);
    } else {
      return array;
    }
  }

  private static class Pair<U> {
    public U transformedElement;
    public U element;

    public Pair(U transformedElement, U element) {
      this.transformedElement = transformedElement;
      this.element = element;
    }
  }
}
