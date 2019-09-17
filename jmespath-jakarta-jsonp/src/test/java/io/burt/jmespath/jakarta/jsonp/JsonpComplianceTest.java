package io.burt.jmespath.jakarta.jsonp;

import io.burt.jmespath.JmesPathComplianceTest;
import io.burt.jmespath.Adapter;

import javax.json.JsonValue;

public class JsonpComplianceTest extends JmesPathComplianceTest<JsonValue> {
  private Adapter<JsonValue> runtime = new JsonpRuntime();

  @Override
  protected Adapter<JsonValue> runtime() { return runtime; }
}
