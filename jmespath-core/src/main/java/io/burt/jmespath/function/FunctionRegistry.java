package io.burt.jmespath.function;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import io.burt.jmespath.Adapter;

/**
 * A collection of functions, used by the runtimes to look up and call functions
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

  /**
   * Creates a new function registry containing the specified functions.
   * When there are multiple functions with the same name the last one is used.
   */
  public FunctionRegistry(JmesPathFunction... functions) {
    this(null, functions);
  }

  private FunctionRegistry(Map<String, JmesPathFunction> functions0, JmesPathFunction... functions1) {
    this.functions = functions0 == null ? new HashMap<String, JmesPathFunction>() : new HashMap<String, JmesPathFunction>(functions0);
    for (JmesPathFunction function : functions1) {
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
  public <T> T callFunction(Adapter<T> runtime, String functionName, List<ExpressionOrValue<T>> arguments) {
    JmesPathFunction function = functions.get(functionName);
    if (function != null) {
      return function.call(runtime, arguments);
    } else {
      throw new FunctionCallException(String.format("Unknown function: \"%s\"", functionName));
    }
  }

  /**
   * Creates a new function registry that contains all the functions of this
   * registry and the specified functions. When a new function has the same name
   * as one of the functions in this registry, the new function will be used.
   */
  public FunctionRegistry extend(JmesPathFunction... functions) {
    return new FunctionRegistry(this.functions, functions);
  }
}
