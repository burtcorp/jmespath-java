package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.JmesPathNode;

public abstract class CompareByFunction extends JmesPathFunction {
  public CompareByFunction() {
    super(2, 2);
  }

  protected abstract boolean sortsBefore(int compareResult);

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "array of objects", "expression");
    }
    if (!secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "expression", adapter.typeOf(arguments.get(1).value()).toString());
    }
    T array = firstArgument.value();
    JmesPathNode expression = secondArgument.expression();
    if (adapter.typeOf(array) != JmesPathType.ARRAY) {
      throw new ArgumentTypeException(name(), "array of objects", adapter.typeOf(array).toString());
    }
    Iterator<T> elements = adapter.toList(array).iterator();
    if (elements.hasNext()) {
      T result = elements.next();
      T resultValue = expression.evaluate(adapter, result);
      boolean expectNumbers = true;
      if (adapter.typeOf(resultValue) == JmesPathType.STRING) {
        expectNumbers = false;
      } else if (adapter.typeOf(resultValue) != JmesPathType.NUMBER) {
        throw new ArgumentTypeException(name(), "number or string", adapter.typeOf(resultValue).toString());
      }
      while (elements.hasNext()) {
        T candidate = elements.next();
        T candidateValue = expression.evaluate(adapter, candidate);
        JmesPathType candidateType = adapter.typeOf(candidateValue);
        if (expectNumbers && candidateType != JmesPathType.NUMBER) {
          throw new ArgumentTypeException(name(), "number", candidateType.toString());
        } else if (!expectNumbers && candidateType != JmesPathType.STRING) {
          throw new ArgumentTypeException(name(), "string", candidateType.toString());
        }
        if (sortsBefore(adapter.compare(candidateValue, resultValue))) {
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
