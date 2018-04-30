package io.burt.jmespath.gson;

import com.google.gson.JsonElement;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeTest;

public class GsonTest extends JmesPathRuntimeTest<JsonElement> {
    private Adapter<JsonElement> runtime = new GsonRuntime();

    @Override
    protected Adapter<JsonElement> runtime() {
        return runtime;
    }
}
