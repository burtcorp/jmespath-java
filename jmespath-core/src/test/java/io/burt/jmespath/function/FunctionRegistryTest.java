package io.burt.jmespath.function;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.jcf.JcfRuntime;

public class FunctionRegistryTest {
  private static final List<String> DEFAULT_FUNCTION_NAMES = Arrays.asList(
    "abs", "avg", "contains", "ceil", "ends_with", "floor", "join", "keys",
    "length", "map", "max", "max_by", "merge", "min", "min_by", "not_null",
    "reverse", "sort", "sort_by", "starts_with", "sum", "to_array", "to_string",
    "to_number", "type", "values"
  );

  private static final List<String> STRING_MANIPULATION_NAMES = Arrays.asList(
    "concat", "lower_case", "matches", "normalize_space", "replace",
    "substring_after", "substring_before", "tokenize", "translate",
    "upper_case"
  );

  private final Adapter<Object> runtime = new JcfRuntime();

  private static class TestFunction extends BaseFunction {
    public TestFunction(String name, ArgumentConstraint argumentConstraints) {
      super(name, argumentConstraints);
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
      return arguments.get(0).value();
    }
  }

  private List<FunctionArgument<Object>> createValueArguments(Object... values) {
    List<FunctionArgument<Object>> arguments = new ArrayList<>();
    for (Object value : values) {
      arguments.add(FunctionArgument.of(value));
    }
    return arguments;
  }

  @Test
  public void theDefaultRegistryContainsTheDefaultFunctions() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    for (String functionName : DEFAULT_FUNCTION_NAMES) {
      assertThat(defaultRegistry.getFunction(functionName).name(), is(functionName));
    }
  }

  @Test
  public void aCustomRegistryDoesNotContainTheDefaultFunctions() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    for (String functionName : DEFAULT_FUNCTION_NAMES) {
      assertThat(customRegistry.getFunction(functionName), is(nullValue()));
    }
  }

  @Test
  public void aCustomRegistryContainsTheProvidedFunctions() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    assertThat(customRegistry.getFunction("foo").name(), is("foo"));
    assertThat(customRegistry.getFunction("bar").name(), is("bar"));
  }

  @Test
  public void theStringManipulationRegistryContainsTheDefaultFunctions() {
    FunctionRegistry defaultRegistry = FunctionRegistry.stringManipulationRegistry();
    for (String functionName : DEFAULT_FUNCTION_NAMES) {
      assertThat(defaultRegistry.getFunction(functionName).name(), is(functionName));
    }
    for (String functionName : STRING_MANIPULATION_NAMES) {
      assertThat(defaultRegistry.getFunction(functionName).name(), is(functionName));
    }
  }

  @Test
  public void theLastFunctionIsUsedWhenThereAreDuplicatedNames() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    Function function = customRegistry.getFunction("foo");
    Object result = function.call(runtime, createValueArguments(3L));
    assertThat(result, is((Object) 3L));
  }

  @Test
  public void extendIsUsedToCreateRegistriesWithExtraFunctions() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    for (String functionName : DEFAULT_FUNCTION_NAMES) {
      assertThat(extendedRegistry.getFunction(functionName).name(), is(functionName));
    }
    assertThat(extendedRegistry.getFunction("foo").name(), is("foo"));
    assertThat(extendedRegistry.getFunction("bar").name(), is("bar"));
  }

  @Test
  public void functionsCanBeOverriddenWithExtend() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("to_number", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    Function function = extendedRegistry.getFunction("to_number");
    Object result = function.call(runtime, createValueArguments("notanumber"));
    assertThat(result, is((Object) "notanumber"));
  }
}
