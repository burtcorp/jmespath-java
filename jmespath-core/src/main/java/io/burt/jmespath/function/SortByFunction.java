package io.burt.jmespath.function;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
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
    Map<T, T> sortedTransformed = new TreeMap<>(adapter);
    Iterator<T> elements = adapter.toList(array).iterator();
    if (elements.hasNext()) {
      T element = elements.next();
      T transformedElement = expression.evaluate(adapter, element);
      boolean expectNumbers = true;
      if (adapter.isString(transformedElement)) {
        expectNumbers = false;
      } else if (!adapter.isNumber(transformedElement)) {
        throw new ArgumentTypeException(name(), "number or string", adapter.typeOf(transformedElement));
      }
      sortedTransformed.put(transformedElement, element);
      while (elements.hasNext()) {
        element = elements.next();
        transformedElement = expression.evaluate(adapter, element);
        if ((expectNumbers && !adapter.isNumber(transformedElement)) || (!expectNumbers && !adapter.isString(transformedElement))) {
          throw new ArgumentTypeException(name(), expectNumbers ? "number" : "string", adapter.typeOf(transformedElement));
        }
        sortedTransformed.put(transformedElement, element);
      }
      return adapter.createArray(new ArrayList<>(sortedTransformed.values()));
    } else {
      List<T> empty = Collections.emptyList();
      return adapter.createArray(empty);
    }
  }
}
