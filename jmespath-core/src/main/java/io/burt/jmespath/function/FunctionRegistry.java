package io.burt.jmespath.function;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import io.burt.jmespath.Adapter;

/**
 * A collection of functions, used by the adapters to look up and call functions
 * by name.
 */
public class FunctionRegistry {
  private final Map<String, JmesPathFunction> functions;

  /**
   * Create a registry with the the functions specified in the JMESPath
   * specification.
   */
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
    registry.add(new MinFunction());
    registry.add(new MinByFunction());
    registry.add(new NotNullFunction());
    registry.add(new ReverseFunction());
    registry.add(new SortFunction());
    registry.add(new SortByFunction());
    registry.add(new StartsWithFunction());
    registry.add(new SumFunction());
    registry.add(new ToArrayFunction());
    registry.add(new ToStringFunction());
    registry.add(new ToNumberFunction());
    registry.add(new TypeFunction());
    registry.add(new ValuesFunction());
    return registry;
  }

  public FunctionRegistry() {
    this.functions = new HashMap<>();
  }

  public void add(JmesPathFunction function) {
    functions.put(function.name(), function);
  }

  /**
   * Call a function by name, passing the specified list of arguments.
   *
   * @throws FunctionCallException when the function is not found
   * @throws ArityException when there are too few or too many arguments
   * @throws ArgumentTypeException when an argument does not match the function's argument type constraints
   */
  public <T> T callFunction(Adapter<T> adapter, String functionName, List<ExpressionOrValue<T>> arguments) {
    JmesPathFunction function = functions.get(functionName);
    if (function != null) {
      return function.call(adapter, arguments);
    } else {
      throw new FunctionCallException(String.format("Unknown function: \"%s\"", functionName));
    }
  }
}
