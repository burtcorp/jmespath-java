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
  protected <T> T callFunction(final Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    List<T> elementsList = runtime.toList(arguments.get(0).value());
    Expression<T> expression = arguments.get(1).expression();
    Iterator<T> elements = elementsList.iterator();
    if (elements.hasNext()) {
      List<Pair<T>> pairs = new ArrayList<>(elementsList.size());
      T element = elements.next();
      T transformedElement = expression.search(element);
      JmesPathType transformedElementType = runtime.typeOf(transformedElement);
      boolean expectNumbers = true;
      if (transformedElementType == JmesPathType.STRING) {
        expectNumbers = false;
      } else if (transformedElementType != JmesPathType.NUMBER) {
        return runtime.handleArgumentTypeError(this, "number or string", transformedElementType.toString());
      }
      pairs.add(new Pair<T>(transformedElement, element));
      while (elements.hasNext()) {
        element = elements.next();
        transformedElement = expression.search(element);
        if (checkType(runtime, transformedElement, expectNumbers)) {
          pairs.add(new Pair<T>(transformedElement, element));
        } else {
          return runtime.handleArgumentTypeError(this, expectNumbers ? "number" : "string", runtime.typeOf(transformedElement).toString());
        }
      }
      return runtime.createArray(sortAndFlatten(runtime, pairs));
    } else {
      return runtime.createArray(new ArrayList<T>());
    }
  }

  private <T> boolean checkType(Adapter<T> runtime, T transformedElement, boolean expectNumbers) {
    JmesPathType elementType = runtime.typeOf(transformedElement);
    return (expectNumbers && elementType == JmesPathType.NUMBER) || (!expectNumbers && elementType == JmesPathType.STRING);
  }

  private <T> List<T> sortAndFlatten(final Adapter<T> runtime, List<Pair<T>> pairs) {
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
    return sorted;
  }

  private static class Pair<U> {
    public final U transformedElement;
    public final U element;

    public Pair(U transformedElement, U element) {
      this.transformedElement = transformedElement;
      this.element = element;
    }
  }
}
