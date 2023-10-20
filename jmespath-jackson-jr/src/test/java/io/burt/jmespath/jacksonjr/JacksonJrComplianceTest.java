/*
 * Copyright 2023 Amazon.com, Inc. or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.burt.jmespath.jacksonjr;

import com.fasterxml.jackson.core.TreeNode;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathComplianceTest;

public class JacksonJrComplianceTest extends JmesPathComplianceTest<TreeNode> {
    private Adapter<TreeNode> runtime = new JacksonJrRuntime();

    @Override
    protected Adapter<TreeNode> runtime() { return runtime; }
}
