package io.burt.jmespath.jcf;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.JmesPathRuntime;

public class JcfTest extends JmesPathRuntimeTest<Object> {
  private JmesPathRuntime<Object> runtime = new JcfRuntime();

  @Override
  protected JmesPathRuntime<Object> runtime() { return runtime; }
}
