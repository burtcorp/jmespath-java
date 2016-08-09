package io.burt.jmespath.jackson;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Iterator;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.burt.jmespath.BaseAdapter;
import io.burt.jmespath.JmesPathType;

public class JacksonAdapter extends BaseAdapter<JsonNode> {
  private final ObjectMapper jsonParser;

  public JacksonAdapter() {
    super();
    this.jsonParser = new ObjectMapper();
  }

  @Override
  public JsonNode parseString(String string) {
    try {
      return jsonParser.readTree(string);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @Override
  public List<JsonNode> toList(JsonNode value) {
    if (value.isArray() || value.isObject()) {
      List<JsonNode> elements = new ArrayList<>(value.size());
      for (JsonNode element : value) {
        elements.add(element);
      }
      return elements;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String toString(JsonNode str) {
    if (str.isTextual()) {
      return str.textValue();
    } else {
      return str.toString();
    }
  }

  @Override
  public Number toNumber(JsonNode n) {
    return n.numberValue();
  }

  @Override
  public boolean isTruthy(JsonNode value) {
    switch (value.getNodeType()) {
      case ARRAY:
      case BINARY:
      case OBJECT:
        return value.size() > 0;
      case STRING:
        return value.textValue().length() > 0;
      case BOOLEAN:
        return value.booleanValue();
      case MISSING:
      case NULL:
        return false;
      case NUMBER:
      case POJO:
        return true;
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getNodeType()));
    }
  }

  @Override
  public JmesPathType typeOf(JsonNode value) {
    switch (value.getNodeType()) {
      case ARRAY:
        return JmesPathType.ARRAY;
      case POJO:
      case OBJECT:
        return JmesPathType.OBJECT;
      case BINARY:
      case STRING:
        return JmesPathType.STRING;
      case BOOLEAN:
        return JmesPathType.BOOLEAN;
      case MISSING:
      case NULL:
        return JmesPathType.NULL;
      case NUMBER:
        return JmesPathType.NUMBER;
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getNodeType()));
    }
  }

  @Override
  public JsonNode getProperty(JsonNode value, String name) {
    return nodeOrNullNode(value.get(name));
  }

  @Override
  public JsonNode getProperty(JsonNode value, JsonNode name) {
    return getProperty(value, name.textValue());
  }

  @Override
  public Collection<JsonNode> getPropertyNames(JsonNode value) {
    if (value.isObject()) {
      List<JsonNode> names = new ArrayList<>(value.size());
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
  public JsonNode createNull() {
    return nodeOrNullNode(null);
  }

  @Override
  public JsonNode createArray(Collection<JsonNode> elements) {
    ArrayNode array = JsonNodeFactory.instance.arrayNode();
    array.addAll(elements);
    return array;
  }

  @Override
  public JsonNode createString(String str) {
    return JsonNodeFactory.instance.textNode(str);
  }

  @Override
  public JsonNode createBoolean(boolean b) {
    return JsonNodeFactory.instance.booleanNode(b);
  }

  @Override
  public JsonNode createObject(Map<JsonNode, JsonNode> obj) {
    ObjectNode object = new ObjectNode(JsonNodeFactory.instance);
    for (Map.Entry<JsonNode, JsonNode> entry : obj.entrySet()) {
      object.set(entry.getKey().textValue(), entry.getValue());
    }
    return object;
  }

  @Override
  public JsonNode createNumber(double n) {
    return JsonNodeFactory.instance.numberNode(n);
  }

  @Override
  public JsonNode createNumber(long n) {
    return JsonNodeFactory.instance.numberNode(n);
  }

  private JsonNode nodeOrNullNode(JsonNode node) {
    if (node == null) {
      return JsonNodeFactory.instance.nullNode();
    } else {
      return node;
    }
  }
}
