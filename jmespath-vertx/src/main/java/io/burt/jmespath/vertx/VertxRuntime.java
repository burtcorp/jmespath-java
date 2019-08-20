package io.burt.jmespath.vertx;

import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.jcf.JcfRuntime;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.burt.jmespath.JmesPathType.ARRAY;
import static io.burt.jmespath.JmesPathType.OBJECT;


public class VertxRuntime extends JcfRuntime {
    public VertxRuntime() {
        this(RuntimeConfiguration.defaultConfiguration());
    }

    public VertxRuntime(RuntimeConfiguration configuration) {
        super(configuration);
    }

    @Override
    public Object parseString(String str) {
        return Json.decodeValue(str);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Object> toList(Object value) {
        if (value instanceof JsonArray) {
            final JsonArray ja = (JsonArray) value;
            return new AbstractList<Object>() {

                @Override
                public int size() {
                    return ja.size();
                }

                @Override
                public Object get(int i) {
                    return ja.getValue(i);
                }
            };
        }
        if (value instanceof JsonObject) {
            List<Object> list = new ArrayList<>(((JsonObject) value).size());
            for (Map.Entry<String, Object> entry : (JsonObject) value ) {
                list.add(entry.getValue());
            }
            return list;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public String toString(Object value) {
        if (value instanceof JsonArray || value instanceof JsonObject) {
            return value.toString();
        } else {
            return super.toString(value);
        }
    }

    @Override
    public boolean isTruthy(Object value) {
        switch (typeOf(value)) {
            case OBJECT:
                return !((JsonObject) value).isEmpty();
            case ARRAY:
                return !((JsonArray) value).isEmpty();
            default:
                return super.isTruthy(value);
        }
    }

    @Override
    public JmesPathType typeOf(Object value) {
        if (value instanceof JsonObject) {
            return OBJECT;
        } else if (value instanceof JsonArray) {
            return ARRAY;
        } else if (value instanceof Collection || value instanceof Map) {
            throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
        } else {
            return super.typeOf(value);
        }
    }

    @Override
    public Object getProperty(Object value, Object name) {
        if ((value instanceof JsonObject) && (name instanceof String)) {
            return ((JsonObject) value).getValue((String) name);
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<Object> getPropertyNames(Object value) {
        if (value instanceof JsonObject) {
            JsonObject jo = (JsonObject) value;
            return (Collection) jo.getMap().keySet();
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    @Override
    public Object createArray(Collection<Object> elements) {
        JsonArray ja = new JsonArray();
        for (Object element : elements) {
            ja.add(element);
        }
        return ja;
    }

    @Override
    public Object createObject(Map<Object, Object> obj) {
        JsonObject jo = new JsonObject();
        for (Map.Entry<Object, Object> entry : obj.entrySet()) {
            jo.put((String) entry.getKey(), entry.getValue());
        }
        return jo;
    }

    @Override
    public int compare(Object value1, Object value2) {
        JmesPathType type1 = typeOf(value1);
        JmesPathType type2 = typeOf(value2);
        if (type1 == type2) {
            switch (type1) {
                case ARRAY:
                case OBJECT:
                    return value1.equals(value2) ? 0 : -1;
                default:
                    return super.compare(value1, value2);
            }
        } else {
            return -1;
        }
    }
}
