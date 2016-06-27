package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToNumberFunction extends JmesPathFunction {
  public ToNumberFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "any value", "expression");
    } else {
      T subject = argument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      if (subjectType == JmesPathType.NUMBER) {
        return subject;
      } else if (subjectType == JmesPathType.STRING) {
        try {
          return adapter.createNumber(Double.parseDouble(adapter.toString(subject)));
        } catch (NumberFormatException nfe) {
          return adapter.createNull();
        }
      } else {
        return adapter.createNull();
      }
    }
  }
}
