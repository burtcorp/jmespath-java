package io.burt.jmespath.jacksonjr;

import com.fasterxml.jackson.core.TreeNode;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathComplianceTest;

public class JacksonJrComplianceTest extends JmesPathComplianceTest<TreeNode> {
    private Adapter<TreeNode> runtime = new JacksonJrRuntime();

    @Override
    protected Adapter<TreeNode> runtime() { return runtime; }
}
