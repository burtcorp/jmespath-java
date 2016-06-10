package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class SortFunction extends JmesPathFunction {
  public SortFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T array = arguments.get(0).value();
    if (TypesHelper.isNumberArray(adapter, array) || TypesHelper.isStringArray(adapter, array)) {
      List<T> elements = new ArrayList(adapter.toList(array));
      Collections.sort(elements, adapter);
      return adapter.createArray(elements);
    } else {
      throw new ArgumentTypeException(name(), "array", adapter.typeOf(array));
    }
  }
}
