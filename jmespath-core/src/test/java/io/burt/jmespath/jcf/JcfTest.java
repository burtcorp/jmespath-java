package io.burt.jmespath.jcf;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.Adapter;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

public class JcfTest extends JmesPathRuntimeTest<Object> {
  @Override
  protected Adapter<Object> createRuntime(RuntimeConfiguration configuration) { return new JcfRuntime(configuration); }

  @Test
  public void toListReturnsAListWhenGivenAnotherTypeOfCollection() {
    List<Object> list = runtime().toList(Collections.singleton(parse("1")));
    assertThat(list, is(Arrays.asList(parse("1"))));
  }
}
