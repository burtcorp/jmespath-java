package io.burt.jmespath.jakarta.jsonp;

import java.io.StringReader;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.NUMBER;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;

public class JsonpRuntime extends BaseRuntime<JsonValue> {
  private final JsonReaderFactory jsonReaderFactory;

  public JsonpRuntime() {
    this(RuntimeConfiguration.defaultConfiguration());
  }

  public JsonpRuntime(RuntimeConfiguration configuration) {
    this(configuration, Json.createReaderFactory(null));
  }

  public JsonpRuntime(RuntimeConfiguration configuration, JsonReaderFactory jsonReaderFactory) {
    super(configuration);
    this.jsonReaderFactory = jsonReaderFactory;
  }

  @Override
  public JsonValue parseString(String string) {
    return jsonReaderFactory.createReader(new StringReader(string)).readValue();
  }

  private static class JsonArrayListWrapper extends AbstractList<JsonValue> {
    private final JsonArray array;

    JsonArrayListWrapper(JsonArray array) {
      this.array = array;
    }

    @Override
    public JsonValue get(int index) {
      return array.get(index);
    }

    @Override
    public int size() {
      return array.size();
    }
  }

  @Override
  public List<JsonValue> toList(JsonValue value) {
    ValueType valueType = value.getValueType();
    if (valueType == ARRAY) {
      return new JsonArrayListWrapper((JsonArray) value);
    } else if (valueType == OBJECT) {
      JsonObject obj = (JsonObject)value;
      if (!obj.isEmpty()) {
        List<JsonValue> elements = new ArrayList<>(obj.size());
        for (JsonValue v : obj.values()) {
          elements.add(useJsonNull(v));
        }
        return elements;
      }
    }
    return Collections.emptyList();
  }

  @Override
  public String toString(JsonValue str) {
    if (str.getValueType() == STRING) {
      return ((JsonString)str).getString();
    } else {
      return str.toString();
    }
  }

  @Override
  public Number toNumber(JsonValue n) {
    return (n.getValueType() == NUMBER) ? ((JsonNumber)n).numberValue() : null;
  }

  @Override
  public boolean isTruthy(JsonValue value) {
    switch (value.getValueType()) {
      case FALSE:
      case NULL:
        return false;
      case NUMBER:
      case TRUE:
        return true;
      case ARRAY:
        return ((JsonArray)value).size() > 0;
      case OBJECT:
        return ((JsonObject)value).size() > 0;
      case STRING:
        return ((JsonString)value).getString().length() > 0;
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getValueType()));
    }
  }

  @Override
  public JmesPathType typeOf(JsonValue value) {
    switch (value.getValueType()) {
      case ARRAY:
        return JmesPathType.ARRAY;
      case OBJECT:
        return JmesPathType.OBJECT;
      case STRING:
        return JmesPathType.STRING;
      case FALSE:
      case TRUE:
        return JmesPathType.BOOLEAN;
      case NULL:
        return JmesPathType.NULL;
      case NUMBER:
        return JmesPathType.NUMBER;
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getValueType()));
    }
  }

  @Override
  public JsonValue getProperty(JsonValue value, JsonValue name) {
    if (value.getValueType() == OBJECT) {
      return nodeOrNullNode(((JsonObject)value).get(textOnStringOrNull(name)));
    } else {
      return JsonValue.NULL;
    }
  }

  @Override
  public Collection<JsonValue> getPropertyNames(JsonValue value) {
    if (value.getValueType() == OBJECT) {
      Set<String> nameSet = ((JsonObject)value).keySet();
      List<JsonValue> names = new ArrayList<>(nameSet.size());
      for(String n : nameSet) {
        names.add(createString(n));
      }
      return names;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public JsonValue createNull() {
    return nodeOrNullNode(null);
  }

  @Override
  public JsonValue createArray(Collection<JsonValue> elements) {
    JsonArrayBuilder builder = Json.createArrayBuilder();
    for(JsonValue element : elements) {
      builder.add(useJsonNull(element));
    }
    return builder.build();
  }

  @Override
  public JsonValue createString(String str) {
    return useJsonNull(Json.createValue(str));
  }

  @Override
  public JsonValue createBoolean(boolean b) {
    return b ? JsonValue.TRUE : JsonValue.FALSE;
  }

  @Override
  public JsonValue createObject(Map<JsonValue, JsonValue> obj) {
    JsonObjectBuilder builder = Json.createObjectBuilder();
    for (Map.Entry<JsonValue, JsonValue> entry : obj.entrySet()) {
      String key = textOnStringOrNull(entry.getKey());
      if (key != null) {
        builder.add(key, useJsonNull(entry.getValue()));
      }
    }
    return builder.build();
  }

  @Override
  public JsonValue createNumber(double n) {
    return Json.createValue(n);
  }

  @Override
  public JsonValue createNumber(long n) {
    return Json.createValue(n);
  }

  private JsonValue useJsonNull(JsonValue value) { return (value == null) ? JsonValue.NULL : value; }

  private JsonValue nodeOrNullNode(JsonValue node) {
    if (node == null) {
      return JsonValue.NULL;
    } else {
      return node;
    }
  }

  private String textOnStringOrNull(JsonValue value) {
    if (value.getValueType() == STRING) {
      return ((JsonString)value).getString();
    } else {
      return null;
    }
  }
}
