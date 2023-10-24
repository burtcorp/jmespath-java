package io.burt.jmespath.gson;

import com.google.gson.JsonElement;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathComplianceTest;

public class GsonComplianceTest extends JmesPathComplianceTest<JsonElement> {
    private final Adapter<JsonElement> runtime = new GsonRuntime();

    @Override
    protected Adapter<JsonElement> runtime() {
        return runtime;
    }
}
