package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.Expression;

/**
 * Helper base class for higher order comparison functions like sort_by, max_by and min_by.
 */
public abstract class ComparingFunction extends BaseFunction {
  public ComparingFunction() {
    super(
      ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT)),
      ArgumentConstraints.expression()
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    List<T> elementsList = runtime.toList(arguments.get(0).value());
    Iterator<T> elements = elementsList.iterator();
    Expression<T> expression = arguments.get(1).expression();
    if (elements.hasNext()) {
      T element = elements.next();
      T elementValue = expression.search(element);
      JmesPathType elementValueType = runtime.typeOf(elementValue);
      boolean expectNumbers = true;
      if (elementValueType == JmesPathType.STRING) {
        expectNumbers = false;
      } else if (elementValueType != JmesPathType.NUMBER) {
        return runtime.handleArgumentTypeError(this, "number or string", elementValueType.toString());
      }
      Aggregator<T> aggregator = createAggregator(runtime, element, elementValue);
      while (elements.hasNext()) {
        T candidate = elements.next();
        T candidateValue = expression.search(candidate);
        if (checkType(runtime, candidateValue, expectNumbers)) {
          aggregator.aggregate(candidate, candidateValue);
        } else {
          return runtime.handleArgumentTypeError(this, expectNumbers ? "number" : "string", runtime.typeOf(candidateValue).toString());
        }
      }
      return aggregator.result();
    } else {
      return createNullValue(runtime);
    }
  }

  protected abstract <T> Aggregator<T> createAggregator(Adapter<T> runtime, T element, T elementValue);

  protected abstract <T> T createNullValue(Adapter<T> runtime);

  public static abstract class Aggregator<V> {
    protected final Adapter<V> runtime;

    public Aggregator(Adapter<V> runtime) {
      this.runtime = runtime;
    }

    protected abstract void aggregate(V candidate, V candidateValue);

    protected abstract V result();
  }

  private <T> boolean checkType(Adapter<T> runtime, T candidateValue, boolean expectNumbers) {
    JmesPathType candidateType = runtime.typeOf(candidateValue);
    return (expectNumbers && candidateType == JmesPathType.NUMBER) || (!expectNumbers && candidateType == JmesPathType.STRING);
  }
}
