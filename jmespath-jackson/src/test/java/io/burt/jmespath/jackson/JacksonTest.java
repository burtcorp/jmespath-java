package io.burt.jmespath.jackson;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.AdapterTest;
import io.burt.jmespath.Adapter;

public class JacksonTest extends AdapterTest<JsonNode> {
  private Adapter<JsonNode> adapter = new JacksonAdapter();

  @Override
  protected Adapter<JsonNode> adapter() { return adapter; }
}
