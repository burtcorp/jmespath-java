package io.burt.jmespath.jacksonjr;

import com.fasterxml.jackson.jr.stree.JrsValue;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathComplianceTest;

public class JacksonJrComplianceTest extends JmesPathComplianceTest<JrsValue> {
    private Adapter<JrsValue> runtime = new JacksonJrRuntime();

    @Override
    protected Adapter<JrsValue> runtime() { return runtime; }
}
