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

import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

import static com.fasterxml.jackson.databind.node.JsonNodeType.*;

public class JacksonAdapter implements Adapter<JsonNode> {
  private final ObjectMapper jsonParser;
  private final FunctionRegistry functionRegistry;

  public JacksonAdapter() {
    this.jsonParser = new ObjectMapper();
    this.functionRegistry = FunctionRegistry.createDefaultRegistry();
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
    if (isString(str)) {
      return str.textValue();
    } else {
      return str.toString();
    }
  }

  @Override
  public Double toDouble(JsonNode n) {
    return n.doubleValue();
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
  public boolean isBoolean(JsonNode value) {
    return value.isBoolean();
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
  public boolean isString(JsonNode value) {
    return value.isTextual();
  }

  @Override
  public String typeOf(JsonNode value) {
    switch (value.getNodeType()) {
      case ARRAY: return "array";
      case POJO:
      case OBJECT:
        return "object";
      case BINARY:
      case STRING:
        return "string";
      case BOOLEAN:
        return "boolean";
      case MISSING:
      case NULL:
        return "null";
      case NUMBER:
        return "number";
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getNodeType()));
    }
  }

  @Override
  public int compare(JsonNode value1, JsonNode value2) {
    if (value1.getNodeType() == value2.getNodeType()) {
      switch (value1.getNodeType()) {
        case ARRAY:
        case BINARY:
        case OBJECT:
        case BOOLEAN:
        case MISSING:
        case NULL:
        case POJO:
          return value1.equals(value2) ? 0 : -1;
        case STRING:
          return value1.textValue().compareTo(value2.textValue());
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
  public Collection<String> getPropertyNames(JsonNode value) {
    if (isObject(value)) {
      List<String> names = new ArrayList<>(value.size());
      Iterator<String> fieldNames = value.fieldNames();
      while (fieldNames.hasNext()) {
        names.add(fieldNames.next());
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
  public JsonNode createArray(List<JsonNode> elements) {
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
  public JsonNode createObject(Map<String, JsonNode> obj) {
    return new ObjectNode(JsonNodeFactory.instance, obj);
  }

  @Override
  public JsonNode createNumber(double d) {
    return JsonNodeFactory.instance.numberNode(d);
  }

  @Override
  public JsonNode callFunction(String name, List<ExpressionOrValue<JsonNode>> arguments) {
    return functionRegistry.callFunction(this, name, arguments);
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
