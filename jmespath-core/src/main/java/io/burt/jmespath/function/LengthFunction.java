package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class LengthFunction extends Function {
  public LengthFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.ARRAY, JmesPathType.OBJECT));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (runtime.typeOf(subject) == JmesPathType.STRING) {
      return runtime.createNumber((long) runtime.toString(subject).length());
    } else {
      return runtime.createNumber((long) runtime.toList(subject).size());
    }
  }
}
