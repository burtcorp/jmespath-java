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
  public List<JsonNode> explode(JsonNode value) {
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
  public JsonNode combine(List<JsonNode> elements) {
    ArrayNode array = JsonNodeFactory.instance.arrayNode();
    for (JsonNode element : elements) {
      if (!element.isNull()) {
        array.add(element);
      }
    }
    return array;
  }

  @Override
  public JsonNode getProperty(JsonNode value, String name) {
    return nodeOrNullNode(value.get(name));
  }

  @Override
  public JsonNode getIndex(JsonNode value, int index) {
    return nodeOrNullNode(value.get(index));
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

  private JsonNode nodeOrNullNode(JsonNode node) {
    if (node == null) {
      return JsonNodeFactory.instance.nullNode();
    } else {
      return node;
    }
  }
}
