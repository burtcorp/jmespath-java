package io.burt.jmespath.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GsonRuntime extends BaseRuntime<JsonElement> {
  private final JsonParser parser;

  public GsonRuntime() {
    this(RuntimeConfiguration.defaultConfiguration());
  }

  public GsonRuntime(RuntimeConfiguration configuration) {
    super(configuration);
    this.parser = new JsonParser();
  }

  @Override
  public JsonElement parseString(String str) {
    return parser.parse(str);
  }

  private static class JsonArrayListWrapper extends AbstractList<JsonElement> {
    private final JsonArray array;

    JsonArrayListWrapper(JsonArray array) {
      this.array = array;
    }

    @Override
    public JsonElement get(int index) {
      return array.get(index);
    }

    @Override
    public int size() {
      return array.size();
    }
  }

  @Override
  public List<JsonElement> toList(JsonElement value) {
    if (value.isJsonArray()) {
      return new JsonArrayListWrapper(value.getAsJsonArray());
    } else if (value.isJsonObject()) {
      JsonObject object = value.getAsJsonObject();
      List<JsonElement> list = new ArrayList<>(object.size());
      for(Map.Entry<String, JsonElement> entry : object.entrySet()) {
        list.add(entry.getValue());
      }
      return list;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String toString(JsonElement value) {
    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
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
        return !value.getAsString().isEmpty();
      case NUMBER:
        return true;
      case ARRAY:
        return !value.getAsJsonArray().isEmpty();
      case OBJECT:
        return !value.getAsJsonObject().isEmpty();
    }
    throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass()));
  }

  @Override
  public JmesPathType typeOf(JsonElement value) {
    if (value.isJsonArray()) {
      return JmesPathType.ARRAY;
    } else if (value.isJsonObject()) {
      return JmesPathType.OBJECT;
    } else if (value.isJsonPrimitive()) {
      if (value.getAsJsonPrimitive().isBoolean()) {
        return JmesPathType.BOOLEAN;
      } else if (value.getAsJsonPrimitive().isNumber()) {
        return JmesPathType.NUMBER;
      } else if (value.getAsJsonPrimitive().isString()) {
        return JmesPathType.STRING;
      }
    } else if (value.isJsonNull()) {
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
    if (value.isJsonObject()) {
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
    for (JsonElement e : elements) {
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
    JsonObject object = new JsonObject();
    for (Map.Entry<JsonElement, JsonElement> entry : obj.entrySet()) {
      object.add(entry.getKey().getAsString(), entry.getValue());
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
