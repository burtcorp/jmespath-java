package io.burt.jmespath.function;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class SortByFunction extends BaseFunction {
  public SortByFunction() {
    super(
      ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT)),
      ArgumentConstraints.expression()
    );
  }

  @Override
  protected <T> T callFunction(final Adapter<T> runtime, List<ValueOrExpression<T>> arguments) {
    List<T> elementsList = runtime.toList(arguments.get(0).value());
    Expression<T> expression = arguments.get(1).expression();
    Iterator<T> elements = elementsList.iterator();
    if (elements.hasNext()) {
      List<Pair<T>> pairs = new ArrayList<>(elementsList.size());
      T element = elements.next();
      T transformedElement = expression.search(element);
      boolean expectNumbers = true;
      JmesPathType elementType = runtime.typeOf(transformedElement);
      if (elementType == JmesPathType.STRING) {
        expectNumbers = false;
      } else if (elementType != JmesPathType.NUMBER) {
        throw new ArgumentTypeException(name(), "number or string", elementType.toString());
      }
      pairs.add(new Pair<T>(transformedElement, element));
      while (elements.hasNext()) {
        element = elements.next();
        transformedElement = expression.search(element);
        elementType = runtime.typeOf(transformedElement);
        if (expectNumbers && elementType != JmesPathType.NUMBER) {
          throw new ArgumentTypeException(name(), "number", elementType.toString());
        } else if (!expectNumbers && elementType != JmesPathType.STRING) {
          throw new ArgumentTypeException(name(), "string", elementType.toString());
        }
        pairs.add(new Pair<T>(transformedElement, element));
      }
      Collections.sort(pairs, new Comparator<Pair<T>>() {
        @Override
        public int compare(Pair<T> a, Pair<T> b) {
          return runtime.compare(a.transformedElement, b.transformedElement);
        }
      });
      List<T> sorted = new ArrayList<>(pairs.size());
      for (Pair<T> pair : pairs) {
        sorted.add(pair.element);
      }
      return runtime.createArray(sorted);
    } else {
      return runtime.createArray(new ArrayList<T>());
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
