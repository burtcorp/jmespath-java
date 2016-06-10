package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class CompareByFunction extends JmesPathFunction {
  private final int compareModifier;

  public CompareByFunction(int compareModifier) {
    super(2, 2);
    this.compareModifier = compareModifier;
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
      T result = elements.next();
      T resultValue = expression.evaluate(adapter, result);
      boolean expectNumbers = true;
      if (adapter.isString(resultValue)) {
        expectNumbers = false;
      } else if (!adapter.isNumber(resultValue)) {
        throw new ArgumentTypeException(name(), "number or string", adapter.typeOf(resultValue));
      }
      while (elements.hasNext()) {
        T candidate = elements.next();
        T candidateValue = expression.evaluate(adapter, candidate);
        if ((expectNumbers && !adapter.isNumber(candidateValue)) || (!expectNumbers && !adapter.isString(candidateValue))) {
          throw new ArgumentTypeException(name(), expectNumbers ? "number" : "string", adapter.typeOf(resultValue));
        }
        if ((compareModifier * adapter.compare(candidateValue, resultValue)) > 0) {
          result = candidate;
          resultValue = candidateValue;
        }
      }
      return result;
    } else {
      return adapter.createNull();
    }
  }
}
