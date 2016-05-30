package io.burt.jmespath.jackson;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.ParseException;

import static com.fasterxml.jackson.databind.node.JsonNodeType.*;

public class JacksonAdapter implements Adapter<JsonNode> {
  private final ObjectMapper jsonParser;

  public JacksonAdapter() {
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
  public boolean isArray(JsonNode value) {
    return value.isArray();
  }

  @Override
  public boolean isObject(JsonNode value) {
    return value.isObject();
  }

  @Override
  public boolean isNumber(JsonNode value) {
    return value.isNumber();
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
  public boolean isNull(JsonNode value) {
    return value.isNull();
  }

  @Override
  public int compare(JsonNode value1, JsonNode value2) {
    if (value1.getNodeType() == value2.getNodeType()) {
      switch (value1.getNodeType()) {
        case ARRAY:
        case BINARY:
        case OBJECT:
        case STRING:
        case BOOLEAN:
        case MISSING:
        case NULL:
        case POJO:
          return value1.equals(value2) ? 0 : -1;
        case NUMBER:
          return Double.compare(value1.doubleValue(), value2.doubleValue());
        default:
          throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getNodeType()));
      }
    } else {
      return -1;
    }
  }

  @Override
  public JsonNode getProperty(JsonNode value, String name) {
    return nodeOrNullNode(value.get(name));
  }

  @Override
  public JsonNode createNull() {
    return nodeOrNullNode(null);
  }

  @Override
  public JsonNode createArray(List<JsonNode> elements) {
    return createArray(elements, false);
  }

  @Override
  public JsonNode createArray(List<JsonNode> elements, boolean compact) {
    ArrayNode array = JsonNodeFactory.instance.arrayNode();
    if (compact) {
      for (JsonNode element : elements) {
        if (!element.isNull()) {
          array.add(element);
        }
      }
    } else {
      array.addAll(elements);
    }
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
  public JsonNode createObject(Map<String, JsonNode> obj) {
    return new ObjectNode(JsonNodeFactory.instance, obj);
  }

  private JsonNode nodeOrNullNode(JsonNode node) {
    if (node == null) {
      return JsonNodeFactory.instance.nullNode();
    } else {
      return node;
    }
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof JacksonAdapter;
  }
}
