package io.burt.jmespath.vertx;

import io.vertx.core.json.JsonObject;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.Adapter;

public class VertxTest extends JmesPathRuntimeTest<Object> {
  @Override
  protected Adapter<Object> createRuntime(RuntimeConfiguration configuration) { return new VertxRuntime(configuration); }
}
