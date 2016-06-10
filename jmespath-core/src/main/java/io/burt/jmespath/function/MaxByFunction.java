package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class MaxByFunction extends JmesPathFunction {
  public MaxByFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    JmesPathNode expression = arguments.get(1).expression();
    if (arguments.get(0).isExpression()) {
      throw new ArgumentTypeException(name(), "array of objects", "expression");
    } else if (!adapter.isArray(array)) {
      throw new ArgumentTypeException(name(), "array of objects", adapter.typeOf(array));
    }
    if (arguments.get(1).isValue()) {
      throw new ArgumentTypeException(name(), "expression", adapter.typeOf(arguments.get(1).value()));
    }
    Iterator<T> elements = adapter.toList(array).iterator();
    if (elements.hasNext()) {
      T max = elements.next();
      T maxValue = expression.evaluate(adapter, max);
      boolean expectNumbers = true;
      if (adapter.isString(maxValue)) {
        expectNumbers = false;
      } else if (!adapter.isNumber(maxValue)) {
        throw new ArgumentTypeException(name(), "number or string", adapter.typeOf(maxValue));
      }
      while (elements.hasNext()) {
        T candidate = elements.next();
        T candidateValue = expression.evaluate(adapter, candidate);
        if ((expectNumbers && !adapter.isNumber(candidateValue)) || (!expectNumbers && !adapter.isString(candidateValue))) {
          throw new ArgumentTypeException(name(), expectNumbers ? "number" : "string", adapter.typeOf(maxValue));
        }
        if (adapter.compare(candidateValue, maxValue) > 0) {
          max = candidate;
          maxValue = candidateValue;
        }
      }
      return max;
    } else {
      return adapter.createNull();
    }
  }
}
