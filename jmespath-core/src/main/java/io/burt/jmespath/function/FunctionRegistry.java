package io.burt.jmespath.function;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import io.burt.jmespath.Adapter;

public class FunctionRegistry {
  private final Map<String, JmesPathFunction> functions;

  public static FunctionRegistry createDefaultRegistry() {
    FunctionRegistry registry = new FunctionRegistry();
    registry.add(new AbsFunction());
    registry.add(new AvgFunction());
    registry.add(new ContainsFunction());
    registry.add(new CeilFunction());
    registry.add(new EndsWithFunction());
    registry.add(new FloorFunction());
    registry.add(new JoinFunction());
    registry.add(new KeysFunction());
    registry.add(new LengthFunction());
    registry.add(new MapFunction());
    registry.add(new MaxFunction());
    registry.add(new MaxByFunction());
    registry.add(new MergeFunction());
    registry.add(new NotNullFunction());
    registry.add(new TypeFunction());
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
