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

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.stree.JacksonJrsTreeCodec;
import com.fasterxml.jackson.jr.stree.JrsArray;
import com.fasterxml.jackson.jr.stree.JrsBoolean;
import com.fasterxml.jackson.jr.stree.JrsNull;
import com.fasterxml.jackson.jr.stree.JrsNumber;
import com.fasterxml.jackson.jr.stree.JrsObject;
import com.fasterxml.jackson.jr.stree.JrsString;
import com.fasterxml.jackson.jr.stree.JrsValue;
import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JacksonJrRuntime extends BaseRuntime<TreeNode> {
    private final JSON json;
    public JacksonJrRuntime() {
        this(RuntimeConfiguration.defaultConfiguration());
    }

    public JacksonJrRuntime(RuntimeConfiguration configuration) {
        this(configuration, JSON.builder()
                .treeCodec(new JacksonJrsTreeCodec())
                .build());
    }
    public JacksonJrRuntime(RuntimeConfiguration configuration, JSON json) {
        super(configuration);
        this.json = json;
    }

    @Override
    public TreeNode parseString(String str) {
        try {
            return json.treeFrom(str);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class JrsArrayListWrapper extends AbstractList<TreeNode> {
        private final JrsArray array;

        JrsArrayListWrapper(JrsArray array) {
            this.array = array;
        }

        @Override
        public TreeNode get(int index) {
            return array.get(index);
        }

        @Override
        public int size() {
            return array.size();
        }
    }

    @Override
    public List<TreeNode> toList(TreeNode value) {
        if (value == null) {
            return Collections.emptyList();
        }
        if (value.isArray()) {
            return new JrsArrayListWrapper((JrsArray) value);
        } else if (value.isObject()) {
            JrsObject object = (JrsObject) value;
            List<TreeNode> list = new ArrayList<>(object.size());
            Iterator<Map.Entry<String, JrsValue>> iterator = object.fields();

            while (iterator.hasNext()) {
                Map.Entry<String, TreeNode> entry = (Map.Entry) iterator.next();
                list.add(entry.getValue());
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString(TreeNode value) {
        if (value.asToken().equals(JsonToken.VALUE_STRING)) {
            return ((JrsString) value).asText();
        } else {
            try {
                return json.asString(value);
            } catch (IOException e) {
                return "";
            }
        }
    }

    @Override
    public Number toNumber(TreeNode value) {
        if (value.isValueNode() && ((JrsValue) value).isNumber()) {
            JrsNumber number = (JrsNumber) value;
            return number.getValue();
        } else return null;
    }

    @Override
    public boolean isTruthy(TreeNode value) {
        // false, null, empty lists, empty objects, empty strings.
        if (value.isContainerNode()) {
            return value.size() > 0;
        } else if (value.isValueNode()) {
            if (value.asToken().equals(JsonToken.VALUE_STRING)) {
                return !((JrsString) value).asText().isEmpty();
            } else return !value.asToken().equals(JsonToken.VALUE_FALSE) &&
                    !value.asToken().equals(JsonToken.VALUE_NULL);
        } else {
            return !value.isMissingNode();
        }
    }

    @Override
    public JmesPathType typeOf(TreeNode value) {
        switch (value.asToken()) {
            case START_ARRAY:
            case END_ARRAY:
                return JmesPathType.ARRAY;
            case VALUE_EMBEDDED_OBJECT:
            case START_OBJECT:
            case END_OBJECT:
                return JmesPathType.OBJECT;
            case VALUE_STRING:
                return JmesPathType.STRING;
            case VALUE_NUMBER_INT:
            case VALUE_NUMBER_FLOAT:
                return JmesPathType.NUMBER;
            case VALUE_TRUE:
            case VALUE_FALSE:
                return JmesPathType.BOOLEAN;
            case VALUE_NULL:
                return JmesPathType.NULL;
            case NOT_AVAILABLE:
            default:
               throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.asToken()));
        }
    }

    @Override
    public TreeNode getProperty(TreeNode value, TreeNode name) {
        if (value == null || value.asToken().equals(JsonToken.VALUE_NULL)) {
            return JrsNull.instance();
        } else {
            TreeNode node = value.get(((JrsString) name).asText());
            return node != null ? node : createNull();
        }
    }

    @Override
    public Collection<TreeNode> getPropertyNames(TreeNode value) {
        if (value != null && value.isObject()) {
            List<TreeNode> names = new ArrayList<>(value.size());
            Iterator<String> fieldNames = value.fieldNames();
            while (fieldNames.hasNext()) {
                names.add(createString(fieldNames.next()));
            }
            return names;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public TreeNode createNull() {
        return JrsNull.instance();
    }

    @Override
    public TreeNode createArray(Collection<TreeNode> elements) {
        List<JrsValue> values = new ArrayList<>();
        for (TreeNode node: elements) {
            if (node == null) {
                values.add(JrsNull.instance());
            } else {
                values.add((JrsValue) node);
            }
        }
        return new JrsArray(values);

    }

    @Override
    public TreeNode createString(String str) {
        return new JrsString(str);
    }

    @Override
    public TreeNode createBoolean(boolean b) {
        return b ? JrsBoolean.TRUE : JrsBoolean.FALSE;
    }

    @Override
    public TreeNode createObject(Map<TreeNode, TreeNode> obj) {
        Map<String, JrsValue> values = new HashMap<>();
        for (Map.Entry<TreeNode, TreeNode> entry : obj.entrySet()) {
            values.put(((JrsString)entry.getKey()).asText(), (JrsValue) entry.getValue());
        }
        return new JrsObject(values);
    }

    @Override
    public TreeNode createNumber(double n) {
        return new JrsNumber(n);
    }

    @Override
    public TreeNode createNumber(long n) {
        return new JrsNumber(n);
    }
}
