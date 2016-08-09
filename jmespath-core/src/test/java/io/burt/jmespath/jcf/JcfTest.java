package io.burt.jmespath.jcf;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.Adapter;

public class JcfTest extends JmesPathRuntimeTest<Object> {
  private Adapter<Object> runtime = new JcfRuntime();

  @Override
  protected Adapter<Object> runtime() { return runtime; }
}
