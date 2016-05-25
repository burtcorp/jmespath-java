package io.burt.jmespath.jackson;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.burt.jmespath.Adapter;

public class JacksonAdapter implements Adapter<JsonNode> {
  @Override
  public List<JsonNode> toList(JsonNode value) {
    if (value.isArray()) {
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

  private JsonNode nodeOrNullNode(JsonNode node) {
    if (node == null) {
      return JsonNodeFactory.instance.nullNode();
    } else {
      return node;
    }
  }
}
