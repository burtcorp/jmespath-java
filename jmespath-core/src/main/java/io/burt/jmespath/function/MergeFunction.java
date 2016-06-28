package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(minArity = 1, maxArity = Integer.MAX_VALUE)
public class MergeFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    if (isObjectArray(adapter, arguments)) {
      Map<T, T> accumulator = new LinkedHashMap<>();
      for (ExpressionOrValue<T> argument : arguments) {
        T value = argument.value();
        for (T property : adapter.getPropertyNames(value)) {
          accumulator.put(property, adapter.getProperty(value, property));
        }
      }
      return adapter.createObject(accumulator);
    } else {
      List<String> types = new ArrayList<>(arguments.size());
      for (ExpressionOrValue<T> argument : arguments) {
        if (argument.isExpression()) {
          types.add("expression");
        } else {
          types.add(adapter.typeOf(argument.value()).toString());
        }
      }
      throw new ArgumentTypeException(name(), "array of object", types.toString());
    }
  }

  private <T> boolean isObjectArray(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    for (ExpressionOrValue<T> argument : arguments) {
      if (argument.isExpression() || adapter.typeOf(argument.value()) != JmesPathType.OBJECT) {
        return false;
      }
    }
    return true;
  }
}
