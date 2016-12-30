package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.Expression;

/**
 * Helper base class for higher order comparison functions like max_by and min_by.
 */
public abstract class CompareByFunction extends BaseFunction {
  public CompareByFunction() {
    super(
      ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT)),
      ArgumentConstraints.expression()
    );
  }

  /**
   * Subclasses override this method to decide whether the greatest or least
   * element sorts first.
   */
  protected abstract boolean sortsBefore(int compareResult);

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    Iterator<T> elements = runtime.toList(arguments.get(0).value()).iterator();
    Expression<T> expression = arguments.get(1).expression();
    if (elements.hasNext()) {
      T result = elements.next();
      T resultValue = expression.search(result);
      JmesPathType resultValueType = runtime.typeOf(resultValue);
      boolean expectNumbers = true;
      if (resultValueType == JmesPathType.STRING) {
        expectNumbers = false;
      } else if (resultValueType != JmesPathType.NUMBER) {
        return runtime.handleArgumentTypeError(this, "number or string", resultValueType.toString());
      }
      while (elements.hasNext()) {
        T candidate = elements.next();
        T candidateValue = expression.search(candidate);
        if (checkType(runtime, candidateValue, expectNumbers)) {
          if (sortsBefore(runtime.compare(candidateValue, resultValue))) {
            result = candidate;
            resultValue = candidateValue;
          }
        } else {
          return runtime.handleArgumentTypeError(this, expectNumbers ? "number" : "string", runtime.typeOf(candidateValue).toString());
        }
      }
      return result;
    } else {
      return runtime.createNull();
    }
  }

  private <T> boolean checkType(Adapter<T> runtime, T candidateValue, boolean expectNumbers) {
    JmesPathType candidateType = runtime.typeOf(candidateValue);
    return (expectNumbers && candidateType == JmesPathType.NUMBER) || (!expectNumbers && candidateType == JmesPathType.STRING);
  }
}
