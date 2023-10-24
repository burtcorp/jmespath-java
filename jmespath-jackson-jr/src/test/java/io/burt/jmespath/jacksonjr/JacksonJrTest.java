package io.burt.jmespath.jacksonjr;


import com.fasterxml.jackson.jr.stree.JrsValue;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;

public class JacksonJrTest extends JmesPathRuntimeTest<JrsValue> {
    @Override
    protected Adapter<JrsValue> createRuntime(RuntimeConfiguration configuration) {
        return new JacksonJrRuntime(configuration);
    }
}
