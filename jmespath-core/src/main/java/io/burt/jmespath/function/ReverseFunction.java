package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class ReverseFunction extends JmesPathFunction {
  public ReverseFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    if (adapter.isArray(array)) {
      List<T> elements = new ArrayList(adapter.toList(array));
      Collections.reverse(elements);
      return adapter.createArray(elements);
    } else {
      throw new ArgumentTypeException(name(), "array", adapter.typeOf(array));
    }
  }
}