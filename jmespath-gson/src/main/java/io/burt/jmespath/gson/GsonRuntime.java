package io.burt.jmespath.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.JmesPathType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GsonRuntime extends BaseRuntime<JsonElement> {
    private final JsonParser parser;

    public GsonRuntime(FunctionRegistry functionRegistry) {
        super(functionRegistry);
        this.parser = new JsonParser();
    }

    public GsonRuntime() {
        this(null);
    }

    @Override
    public JsonElement parseString(String str) {
        return parser.parse(str);
    }

    @Override
    public List<JsonElement> toList(JsonElement value) {
        List<JsonElement> list;
        if(value.isJsonObject()) {
            list = new ArrayList<>(value.getAsJsonObject().size());
            for(Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                list.add(entry.getValue());
            }

            return list;
        }

        if(value.isJsonArray()) {
            list = new ArrayList<>(value.getAsJsonArray().size());
            for(JsonElement e : value.getAsJsonArray()) {
                list.add(e);
            }

            return list;
        }

        return Collections.emptyList();
    }

    @Override
    public String toString(JsonElement value) {
        if(value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            return value.getAsJsonPrimitive().getAsString();
        } else {
            return value.toString();
        }
    }

    @Override
    public Number toNumber(JsonElement value) {
        return (value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) ? value.getAsNumber() : null;
    }

    @Override
    public boolean isTruthy(JsonElement value) {
        switch (typeOf(value)) {
            case NULL:
                return false;
            case BOOLEAN:
                return value.getAsBoolean();
            case STRING:
                return value.getAsString().length() > 0;
            case NUMBER:
                return true;
            case ARRAY:
                return value.getAsJsonArray().size() > 0;
            case OBJECT:
                return value.getAsJsonObject().size() > 0;
        }

        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass()));
    }

    @Override
    public JmesPathType typeOf(JsonElement value) {
        if(value.isJsonArray()) {
            return JmesPathType.ARRAY;
        }

        if(value.isJsonObject()) {
            return JmesPathType.OBJECT;
        }

        if(value.isJsonPrimitive()) {
            if(value.getAsJsonPrimitive().isBoolean()) {
                return JmesPathType.BOOLEAN;
            }

            if(value.getAsJsonPrimitive().isNumber()) {
                return JmesPathType.NUMBER;
            }

            if(value.getAsJsonPrimitive().isString()) {
                return JmesPathType.STRING;
            }
        }

        if(value.isJsonNull()) {
            return JmesPathType.NULL;
        }

        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass()));
    }

    @Override
    public JsonElement getProperty(JsonElement value, JsonElement name) {
        return nodeOrNullNode(
                value.isJsonObject() ? value.getAsJsonObject().get(name.getAsString()) : null
        );
    }

    @Override
    public Collection<JsonElement> getPropertyNames(JsonElement value) {
        if(value.isJsonObject()) {
            JsonObject object = (JsonObject) value;
            List<JsonElement> names = new ArrayList<>((object.size()));

            for (String s : object.keySet()) {
                names.add(createString(s));
            }
            return names;
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public JsonElement createNull() {
        return JsonNull.INSTANCE;
    }

    @Override
    public JsonElement createArray(Collection<JsonElement> elements) {
        JsonArray array = new JsonArray();
        for(JsonElement e : elements) {
            array.add(e);
        }
        return array;
    }

    @Override
    public JsonElement createString(String str) {
        return new JsonPrimitive(str);
    }

    @Override
    public JsonElement createBoolean(boolean b) {
        return new JsonPrimitive(b);
    }

    @Override
    public JsonElement createObject(Map<JsonElement, JsonElement> obj) {
        JsonElement object = new JsonObject();
        for(Map.Entry<JsonElement, JsonElement> entry : obj.entrySet()) {
            ((JsonObject) object).add(entry.getKey().getAsString(), entry.getValue());
        }
        return object;
    }

    @Override
    public JsonElement createNumber(double n) {
        return new JsonPrimitive(n);
    }

    @Override
    public JsonElement createNumber(long n) {
        return new JsonPrimitive(n);
    }

    private JsonElement nodeOrNullNode(JsonElement node) {
        if (node == null) {
            return JsonNull.INSTANCE;
        } else {
            return node;
        }
    }
}
