package io.burt.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.JmesPathRuntime;

public class JacksonTest extends JmesPathRuntimeTest<JsonNode> {
  private JmesPathRuntime<JsonNode> runtime = new JacksonRuntime();

  @Override
  protected JmesPathRuntime<JsonNode> runtime() { return runtime; }
}
