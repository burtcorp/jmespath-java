package io.burt.jmespath.jcf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeWithDefaultConfigurationTest;
import io.burt.jmespath.RuntimeConfiguration;

@RunWith(Enclosed.class)
public class JcfTest {
  public static class DefaultConfiguration extends JmesPathRuntimeWithDefaultConfigurationTest<Object> {
    @Override
    protected Adapter<Object> createRuntime(RuntimeConfiguration configuration) { return new JcfRuntime(configuration); }

    @Test
    public void toListReturnsAListWhenGivenAnotherTypeOfCollection() {
      List<Object> list = runtime().toList(Collections.singleton(parse("1")));
      assertThat(list, is(Arrays.asList(parse("1"))));
    }
  }

  }
}
