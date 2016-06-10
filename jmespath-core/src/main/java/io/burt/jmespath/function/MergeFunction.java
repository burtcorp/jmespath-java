package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import io.burt.jmespath.Adapter;

public class MergeFunction extends JmesPathFunction {
  public MergeFunction() {
    super(1, Integer.MAX_VALUE);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    if (isObjectArray(adapter, arguments)) {
      Map<String, T> accumulator = new LinkedHashMap<>();
      for (ExpressionOrValue<T> argument : arguments) {
        T value = argument.value();
        for (String property : adapter.getPropertyNames(value)) {
          accumulator.put(property, adapter.getProperty(value, property));
        }
      }
      return adapter.createObject(accumulator);
    } else {
      List<String> types = new ArrayList<>(arguments.size());
      for (ExpressionOrValue<T> argument : arguments) {
        types.add(adapter.typeOf(argument.value()));
      }
      throw new ArgumentTypeException(name(), "array of object", types.toString());
    }
  }

  private <T> boolean isObjectArray(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (!adapter.isObject(argument.value())) {
        return false;
      }
    }
    return true;
  }
}