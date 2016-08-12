package io.burt.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.Adapter;

public class JacksonTest extends JmesPathRuntimeTest<JsonNode> {
  private Adapter<JsonNode> runtime = new JacksonRuntime();

  @Override
  protected Adapter<JsonNode> runtime() { return runtime; }
}
