package io.burt.jmespath.gson;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.gson.JsonElement;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeWithDefaultConfigurationTest;
import io.burt.jmespath.JmesPathRuntimeWithStringFunctionTest;
import io.burt.jmespath.RuntimeConfiguration;

@RunWith(Enclosed.class)
public class GsonTest {
  public static class DefaultConfiguration extends JmesPathRuntimeWithDefaultConfigurationTest<JsonElement> {
    @Override
    protected Adapter<JsonElement> createRuntime(RuntimeConfiguration configuration) { return new GsonRuntime(configuration); }
  }

  public static class StringManipulation extends JmesPathRuntimeWithStringFunctionTest<JsonElement> {
    @Override
    protected Adapter<JsonElement> createRuntime(RuntimeConfiguration configuration) { return new GsonRuntime(configuration); }
  }
}

