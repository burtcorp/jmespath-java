package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class LengthFunction extends JmesPathFunction {
  public LengthFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.ARRAY, JmesPathType.OBJECT));
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (adapter.typeOf(subject) == JmesPathType.STRING) {
      return adapter.createNumber((long) adapter.toString(subject).length());
    } else {
      return adapter.createNumber((long) adapter.toList(subject).size());
    }
  }
}
