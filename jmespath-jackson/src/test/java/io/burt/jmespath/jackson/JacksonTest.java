package io.burt.jmespath.jackson;

import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPathRuntimeWithDefaultConfigurationTest;
import io.burt.jmespath.JmesPathRuntimeWithStringFunctionTest;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.Adapter;

@RunWith(Enclosed.class)
public class JacksonTest {
  public static class DefaultConfiguration extends JmesPathRuntimeWithDefaultConfigurationTest<JsonNode> {
    @Override
    protected Adapter<JsonNode> createRuntime(RuntimeConfiguration configuration) { return new JacksonRuntime(configuration); }
  }

  public static class StringManipulation extends JmesPathRuntimeWithStringFunctionTest<JsonNode> {
    @Override
    protected Adapter<JsonNode> createRuntime(RuntimeConfiguration configuration) { return new JacksonRuntime(configuration); }
  }
}
