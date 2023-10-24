package io.burt.jmespath.jacksonjr;

import com.fasterxml.jackson.core.JsonToken;
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
import java.io.UncheckedIOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JacksonJrRuntime extends BaseRuntime<JrsValue> {
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
    public JrsValue parseString(String str) {
        try {
            return json.treeFrom(str);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static class JrsArrayListWrapper extends AbstractList<JrsValue> {
        private final JrsArray array;

        JrsArrayListWrapper(JrsArray array) {
            this.array = array;
        }

        @Override
        public JrsValue get(int index) {
            return array.get(index);
        }

        @Override
        public int size() {
            return array.size();
        }
    }

    @Override
    public List<JrsValue> toList(JrsValue value) {
        if (value == null) {
            return Collections.emptyList();
        } else if (value.isArray()) {
            return new JrsArrayListWrapper((JrsArray) value);
        } else if (value.isObject()) {
            JrsObject object = (JrsObject) value;
            List<JrsValue> list = new ArrayList<>(object.size());
            Iterator<Map.Entry<String, JrsValue>> iterator = object.fields();

            while (iterator.hasNext()) {
                Map.Entry<String, JrsValue> entry = iterator.next();
                list.add(entry.getValue());
            }
            return list;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString(JrsValue value) {
        if (JsonToken.VALUE_STRING.equals(value.asToken())) {
            return value.asText();
        } else {
            try {
                return json.asString(value);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public Number toNumber(JrsValue value) {
        if (value.isValueNode() && value.isNumber()) {
            JrsNumber number = (JrsNumber) value;
            return number.getValue();
        } else {
            return null;
        }
    }

    @Override
    public boolean isTruthy(JrsValue value) {
        if (value.isContainerNode()) {
            return value.size() > 0;
        } else if (value.isValueNode()) {
            switch (value.asToken()) {
                case VALUE_STRING:
                    return !value.asText().isEmpty();
                case VALUE_FALSE:
                case VALUE_NULL:
                    return false;
                default:
                    return true;
            }
        } else {
            return !value.isMissingNode();
        }
    }

    @Override
    public JmesPathType typeOf(JrsValue value) {
        switch (value.asToken()) {
            case START_ARRAY:
                return JmesPathType.ARRAY;
            case VALUE_EMBEDDED_OBJECT:
            case START_OBJECT:
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
    public JrsValue getProperty(JrsValue value, JrsValue name) {
        if (JsonToken.VALUE_NULL.equals(value.asToken())) {
            return JrsNull.instance();
        } else {
            JrsValue node = value.get(name.asText());
            return node != null ? node : createNull();
        }
    }

    @Override
    public Collection<JrsValue> getPropertyNames(JrsValue value) {
        if (value.isObject()) {
            List<JrsValue> names = new ArrayList<>(value.size());
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
    public JrsValue createNull() {
        return JrsNull.instance();
    }

    @Override
    public JrsValue createArray(Collection<JrsValue> elements) {
        List<JrsValue> values = new ArrayList<>();
        for (JrsValue node: elements) {
            if (node == null) {
                values.add(JrsNull.instance());
            } else {
                values.add(node);
            }
        }
        return new JrsArray(values);

    }

    @Override
    public JrsValue createString(String str) {
        return new JrsString(str);
    }

    @Override
    public JrsValue createBoolean(boolean b) {
        return b ? JrsBoolean.TRUE : JrsBoolean.FALSE;
    }

    @Override
    public JrsValue createObject(Map<JrsValue, JrsValue> obj) {
        Map<String, JrsValue> values = new HashMap<>();
        for (Map.Entry<JrsValue, JrsValue> entry : obj.entrySet()) {
            values.put(entry.getKey().asText(), entry.getValue());
        }
        return new JrsObject(values);
    }

    @Override
    public JrsValue createNumber(double n) {
        return new JrsNumber(n);
    }

    @Override
    public JrsValue createNumber(long n) {
        return new JrsNumber(n);
    }
}
