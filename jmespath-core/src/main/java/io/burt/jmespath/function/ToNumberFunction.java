package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToNumberFunction extends BaseFunction {
  public ToNumberFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    JmesPathType subjectType = runtime.typeOf(subject);
    if (subjectType == JmesPathType.NUMBER) {
      return subject;
    } else if (subjectType == JmesPathType.STRING) {
      try {
        return runtime.createNumber(Double.parseDouble(runtime.toString(subject)));
      } catch (NumberFormatException nfe) {
        return runtime.createNull();
      }
    } else {
      return runtime.createNull();
    }
  }
}
