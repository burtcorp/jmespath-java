package io.burt.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPathComplianceTest;
import io.burt.jmespath.Adapter;

public class JacksonComplianceTest extends JmesPathComplianceTest<JsonNode> {
  private final Adapter<JsonNode> runtime = new JacksonRuntime();

  @Override
  protected Adapter<JsonNode> runtime() { return runtime; }
}
