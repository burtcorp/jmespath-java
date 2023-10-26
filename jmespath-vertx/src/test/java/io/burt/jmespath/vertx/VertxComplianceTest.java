package io.burt.jmespath.vertx;

import io.burt.jmespath.JmesPathComplianceTest;
import io.burt.jmespath.Adapter;

public class VertxComplianceTest extends JmesPathComplianceTest<Object> {
  private final Adapter<Object> runtime = new VertxRuntime();

  @Override
  protected Adapter<Object> runtime() { return runtime; }
}
