package io.burt.jmespath.jacksonjr;


import com.fasterxml.jackson.core.TreeNode;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathRuntimeTest;
import io.burt.jmespath.RuntimeConfiguration;

public class JacksonJrTest extends JmesPathRuntimeTest<TreeNode> {
    @Override
    protected Adapter<TreeNode> createRuntime(RuntimeConfiguration configuration) {
        return new JacksonJrRuntime(configuration);
    }
}
