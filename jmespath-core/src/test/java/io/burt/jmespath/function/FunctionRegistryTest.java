package io.burt.jmespath.function;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.ExpressionReferenceNode;
import io.burt.jmespath.node.PropertyNode;
import io.burt.jmespath.node.CurrentNode;
import io.burt.jmespath.jcf.JcfRuntime;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class FunctionRegistryTest {
  private final Adapter<Object> runtime = new JcfRuntime();

  private List<ExpressionOrValue<Object>> createValueArguments(Object... values) {
    List<ExpressionOrValue<Object>> arguments = new ArrayList<>();
    for (Object value : values) {
      arguments.add(new ExpressionOrValue<Object>(value));
    }
    return arguments;
  }

  private static class TestFunction extends JmesPathFunction {
    public TestFunction(String name, ArgumentConstraint argumentConstraints) {
      super(name, argumentConstraints);
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
      return arguments.get(0).value();
    }
  }

  @Test
  public void theDefaultRegistryContainsTheDefaultFunctions() {
    FunctionRegistry registry = FunctionRegistry.defaultRegistry();
    Object result;
    result = registry.callFunction(runtime, "to_string", createValueArguments(1));
    assertThat(result, is((Object) "1"));
    result = registry.callFunction(runtime, "to_number", createValueArguments("1"));
    assertThat(result, is((Object) 1.0));
  }

  @Test
  public void aCustomRegistryDoesNotContainTheDefaultFunctions() {
    FunctionRegistry registry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    try {
      registry.callFunction(runtime, "to_number", createValueArguments(1));
    } catch (FunctionCallException fce) {
      assertThat(fce.getMessage(), containsString("Unknown function: \"to_number\""));
    }
  }

  @Test
  public void aCustomRegistryContainsTheProvidedFunctions() {
    FunctionRegistry registry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    Object result;
    result = registry.callFunction(runtime, "foo", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
    result = registry.callFunction(runtime, "bar", createValueArguments(42));
    assertThat(result, is((Object) 42));
  }

  @Test
  public void theLastFunctionIsUsedWhenThereAreDuplicatedNames() {
    FunctionRegistry registry = new FunctionRegistry(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    registry.callFunction(runtime, "foo", createValueArguments(3));
  }

  @Test
  public void callingAMissingFunctionThrowsFunctionCallException() {
    FunctionRegistry registry = FunctionRegistry.defaultRegistry();
    try {
      registry.callFunction(runtime, "foo", createValueArguments(1, 2, 3));
    } catch (FunctionCallException fce) {
      assertThat(fce.getMessage(), containsString("Unknown function: \"foo\""));
    }
  }

  @Test
  public void extendIsUsedToCreateRegistriesWithExtraFunctions() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("foo", ArgumentConstraints.typeOf(JmesPathType.STRING)),
      new TestFunction("bar", ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    );
    Object result;
    result = extendedRegistry.callFunction(runtime, "to_number", createValueArguments("3"));
    assertThat(result, is((Object) 3.0));
    result = extendedRegistry.callFunction(runtime, "foo", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
    result = extendedRegistry.callFunction(runtime, "bar", createValueArguments(42));
    assertThat(result, is((Object) 42));
  }

  @Test
  public void functionsCanBeOverriddenWithExtend() {
    FunctionRegistry defaultRegistry = FunctionRegistry.defaultRegistry();
    FunctionRegistry extendedRegistry = defaultRegistry.extend(
      new TestFunction("to_number", ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
    Object result = extendedRegistry.callFunction(runtime, "to_number", createValueArguments("hello"));
    assertThat(result, is((Object) "hello"));
  }
}
