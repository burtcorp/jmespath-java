package io.burt.jmespath.gson;

import com.google.gson.JsonElement;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;

public class GsonTest extends JmesPathRuntimeTest<JsonElement> {
  @Override
  protected Adapter<JsonElement> createRuntime(RuntimeConfiguration configuration) { return new GsonRuntime(configuration); }
}
