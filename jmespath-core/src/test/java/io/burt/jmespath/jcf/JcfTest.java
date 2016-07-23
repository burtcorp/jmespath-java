package io.burt.jmespath.jcf;

import io.burt.jmespath.AdapterTest;
import io.burt.jmespath.Adapter;

public class JcfTest extends AdapterTest<Object> {
  private Adapter<Object> adapter = new JcfAdapter();

  @Override
  protected Adapter<Object> adapter() { return adapter; }
}
