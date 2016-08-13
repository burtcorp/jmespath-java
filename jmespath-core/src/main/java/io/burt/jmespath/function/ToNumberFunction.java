package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToNumberFunction extends BaseFunction {
  public ToNumberFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    T subject = arguments.get(0).value();
    JmesPathType subjectType = runtime.typeOf(subject);
    if (subjectType == JmesPathType.NUMBER) {
      return subject;
    } else if (subjectType == JmesPathType.STRING) {
      try {
        double d = Double.parseDouble(runtime.toString(subject));
        if (d == Math.rint(d)) {
          return runtime.createNumber((long) d);
        } else {
          return runtime.createNumber(d);
        }
      } catch (NumberFormatException nfe) {
        return runtime.createNull();
      }
    } else {
      return runtime.createNull();
    }
  }
}
