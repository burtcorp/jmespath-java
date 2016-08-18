package io.burt.jmespath.function;

import org.junit.Test;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.jcf.JcfRuntime;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;

public class FunctionRegistryTest {
  private Adapter<Object> runtime = new JcfRuntime();

  private Object callFunction(String name, List<FunctionArgument<Object>> args) {
    return runtime.functionRegistry().getFunction(name).call(runtime, args);
  }

  private List<FunctionArgument<Object>> createValueArguments(Object... values) {
    List<FunctionArgument<Object>> arguments = new ArrayList<>();
    for (Object value : values) {
      arguments.add(FunctionArgument.of(value));
    }
    return arguments;
  }

  private static class TestFunction extends BaseFunction {
    public TestFunction(String name, ArgumentConstraint argumentConstraints) {
      super(name, argumentConstraints);
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
      return arguments.get(0).value();
    }
  }

  @Test
  public void theDefaultRegistryContainsTheDefaultFunctions() {
    Object result;
    result = callFunction("to_string", createValueArguments(1L));
    assertThat(result, is((Object) "1"));
    result = callFunction("to_number", createValueArguments("1"));
    assertThat(result, is((Object) 1L));
  }

  @Test
  public void aCustomRegistryDoesNotContainTheDefaultFunctions() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    assertThat(customRegistry.getFunction("to_number"), is(equalTo(null)));
  }

  @Test
  public void aCustomRegistryContainsTheProvidedFunctions() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    runtime = new JcfRuntime(customRegistry);
    Object result;
    result = callFunction("foo", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
    result = callFunction("bar", createValueArguments(42L));
    assertThat(result, is((Object) 42L));
  }

  @Test
  public void theLastFunctionIsUsedWhenThereAreDuplicatedNames() {
    FunctionRegistry customRegistry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    runtime = new JcfRuntime(customRegistry);
    callFunction("foo", createValueArguments(3L));
  }

  @Test
  public void extendIsUsedToCreateRegistriesWithExtraFunctions() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    runtime = new JcfRuntime(extendedRegistry);
    Object result;
    result = callFunction("to_number", createValueArguments("3"));
    assertThat(result, is((Object) 3L));
    result = callFunction("foo", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
    result = callFunction("bar", createValueArguments(42L));
    assertThat(result, is((Object) 42L));
  }

  @Test
  public void functionsCanBeOverriddenWithExtend() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("to_number", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    runtime = new JcfRuntime(extendedRegistry);
    Object result = callFunction("to_number", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
  }
}
