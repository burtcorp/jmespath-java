package io.burt.jmespath.jakarta.jsonp;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;

import javax.json.JsonValue;

public class JsonpTest extends JmesPathRuntimeTest<JsonValue> {
  @Override
  protected Adapter<JsonValue> createRuntime(RuntimeConfiguration configuration) { return new JsonpRuntime(configuration); }
}
