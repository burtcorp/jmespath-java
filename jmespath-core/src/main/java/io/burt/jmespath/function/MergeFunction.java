package io.burt.jmespath.function;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class MergeFunction extends JmesPathFunction {
  public MergeFunction() {
    super(ArgumentConstraints.listOf(1, Integer.MAX_VALUE, ArgumentConstraints.typeOf(JmesPathType.OBJECT)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    Map<T, T> accumulator = new LinkedHashMap<>();
    for (ExpressionOrValue<T> argument : arguments) {
      T value = argument.value();
      for (T property : adapter.getPropertyNames(value)) {
        accumulator.put(property, adapter.getProperty(value, property));
      }
    }
    return adapter.createObject(accumulator);
  }
}
