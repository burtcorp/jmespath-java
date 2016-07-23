package io.burt.jmespath.function;

import java.util.List;
import java.util.Arrays;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ToArrayFunction extends JmesPathFunction {
  public ToArrayFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (adapter.typeOf(subject) == JmesPathType.ARRAY) {
      return subject;
    } else {
      return adapter.createArray(Arrays.asList(subject));
    }
  }
}
