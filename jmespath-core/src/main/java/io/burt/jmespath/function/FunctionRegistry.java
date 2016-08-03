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
  private final static FunctionRegistry defaultRegistry = new FunctionRegistry(
    new AbsFunction(),
    new AvgFunction(),
    new ContainsFunction(),
    new CeilFunction(),
    new EndsWithFunction(),
    new FloorFunction(),
    new JoinFunction(),
    new KeysFunction(),
    new LengthFunction(),
    new MapFunction(),
    new MaxFunction(),
    new MaxByFunction(),
    new MergeFunction(),
    new MinFunction(),
    new MinByFunction(),
    new NotNullFunction(),
    new ReverseFunction(),
    new SortFunction(),
    new SortByFunction(),
    new StartsWithFunction(),
    new SumFunction(),
    new ToArrayFunction(),
    new ToStringFunction(),
    new ToNumberFunction(),
    new TypeFunction(),
    new ValuesFunction()
  );

  private final Map<String, JmesPathFunction> functions;

  /**
   * Returns a registry with all the the functions specified in the JMESPath
   * specification.
   */
  public static FunctionRegistry defaultRegistry() {
    return defaultRegistry;
  }

  public FunctionRegistry(JmesPathFunction... functions) {
    this.functions = new HashMap<>();
    for (JmesPathFunction function : functions) {
      this.functions.put(function.name(), function);
    }
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
