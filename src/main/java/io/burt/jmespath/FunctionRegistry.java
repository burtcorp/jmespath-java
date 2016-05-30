package io.burt.jmespath;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import io.burt.jmespath.function.ExpressionOrValue;
import io.burt.jmespath.function.JmesPathFunction;
import io.burt.jmespath.function.TypeFunction;
import io.burt.jmespath.function.MapFunction;

public class FunctionRegistry {
  private final Map<String, JmesPathFunction> functions;

  public static FunctionRegistry createDefaultRegistry() {
    FunctionRegistry registry = new FunctionRegistry();
    registry.add(new TypeFunction());
    registry.add(new MapFunction());
    return registry;
  }

  public FunctionRegistry() {
    this.functions = new HashMap<>();
  }

  public void add(JmesPathFunction function) {
    functions.put(function.name(), function);
  }

  public <T> T callFunction(Adapter<T> adapter, String functionName, List<ExpressionOrValue<T>> arguments) {
    JmesPathFunction function = functions.get(functionName);
    if (function != null) {
      return function.call(adapter, arguments);
    } else {
      throw new FunctionCallException(String.format("Unknown function: \"%s\"", functionName));
    }
  }
}
