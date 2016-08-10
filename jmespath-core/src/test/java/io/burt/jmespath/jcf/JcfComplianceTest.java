package io.burt.jmespath.jcf;

import io.burt.jmespath.JmesPathComplianceTest;
import io.burt.jmespath.Adapter;

public class JcfComplianceTest extends JmesPathComplianceTest<Object> {
  private Adapter<Object> runtime = new JcfRuntime();

  @Override
  protected Adapter<Object> runtime() { return runtime; }
}
