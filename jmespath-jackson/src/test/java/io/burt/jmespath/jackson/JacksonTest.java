package io.burt.jmespath.jackson;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.AdapterTest;
import io.burt.jmespath.Adapter;

import static org.junit.Assert.fail;

public class JacksonTest extends AdapterTest<JsonNode> {
  private Adapter<JsonNode> adapter = new JacksonAdapter();

  @Override
  protected Adapter<JsonNode> adapter() { return adapter; }
}
