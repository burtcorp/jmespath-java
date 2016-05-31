package io.burt.jmespath.jcf;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.io.IOException;

import io.burt.jmespath.AdapterTest;
import io.burt.jmespath.Adapter;

import static org.junit.Assert.fail;

public class JcfTest extends AdapterTest<Object> {
  private Adapter<Object> adapter = new JcfAdapter();

  @Override
  protected Adapter<Object> adapter() { return adapter; }

  @Override
  protected String valueToString(Object node) {
    if (node instanceof String) {
      return node.toString();
    } else {
      return null;
    }
  }
}
