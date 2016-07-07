package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToNumberFunction extends JmesPathFunction {
  public ToNumberFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
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
