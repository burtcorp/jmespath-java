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

  private final Map<String, Function> functions;

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
  public FunctionRegistry(Function... functions) {
    this(null, functions);
  }

  private FunctionRegistry(Map<String, Function> functions0, Function... functions1) {
    this.functions = functions0 == null ? new HashMap<String, Function>() : new HashMap<String, Function>(functions0);
    for (Function function : functions1) {
      this.functions.put(function.name(), function);
    }
  }

  /**
   * Returns the function by the specified name or null if no such function exists.
   */
  public Function getFunction(String functionName) {
    return functions.get(functionName);
  }

  /**
   * Creates a new function registry that contains all the functions of this
   * registry and the specified functions. When a new function has the same name
   * as one of the functions in this registry, the new function will be used.
   */
  public FunctionRegistry extend(Function... functions) {
    return new FunctionRegistry(this.functions, functions);
  }
}
