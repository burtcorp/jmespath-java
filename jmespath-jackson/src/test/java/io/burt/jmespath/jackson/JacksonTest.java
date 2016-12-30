package io.burt.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.Adapter;

public class JacksonTest extends JmesPathRuntimeTest<JsonNode> {
  @Override
  protected Adapter<JsonNode> createRuntime(RuntimeConfiguration configuration) { return new JacksonRuntime(configuration); }
}
