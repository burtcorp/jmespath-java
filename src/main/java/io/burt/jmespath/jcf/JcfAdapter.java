package io.burt.jmespath.jcf;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.Collections;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

public class JcfAdapter implements Adapter<Object> {
  private final FunctionRegistry functionRegistry;

  public JcfAdapter() {
    this.functionRegistry = FunctionRegistry.createDefaultRegistry();
  }

  @Override
  public Object parseString(String string) {
    return JsonParser.fromString(string, this);
  }

  @Override
  public List<Object> toList(Object value) {
    if (value instanceof List) {
      return (List<Object>) value;
    } else if (value instanceof Map) {
      Map<String, Object> object = (Map<String, Object>) value;
      return new ArrayList(object.values());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean isArray(Object value) {
    return value instanceof List;
  }

  @Override
  public boolean isObject(Object value) {
    return value instanceof Map;
  }

  @Override
  public boolean isBoolean(Object value) {
    return value instanceof Boolean;
  }

  @Override
  public boolean isNumber(Object value) {
    return value instanceof Number;
  }

  public boolean isString(Object value) {
    return value instanceof String;
  }

  @Override
  public boolean isTruthy(Object value) {
    if (isNull(value)) {
      return false;
    } else if (isBoolean(value)) {
      return value == Boolean.TRUE;
    } else if (isNumber(value)) {
      return false;
    } else if (isArray(value)) {
      return !((List) value).isEmpty();
    } else if (isObject(value)) {
      return !((Map) value).isEmpty();
    } else if (isString(value)) {
      return !((String) value).isEmpty();
    } else {
      throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  @Override
  public boolean isNull(Object value) {
    return value == null;
  }

  @Override
  public String typeOf(Object value) {
    if (isNull(value)) {
      return "null";
    } else if (isBoolean(value)) {
      return "boolean";
    } else if (isNumber(value)) {
      return "number";
    } else if (isArray(value)) {
      return "array";
    } else if (isObject(value)) {
      return "object";
    } else if (isString(value)) {
      return "string";
    } else {
      throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  @Override
  public int compare(Object value1, Object value2) {
    String type1 = typeOf(value1);
    String type2 = typeOf(value2);
    if (type1.equals(type2)) {
      if (type1.equals("null")) {
        return 0;
      } else if (type1.equals("boolean")) {
        return value1 == value2 ? 0 : -1;
      } else if (type1.equals("number")) {
        double d1 = (double) value1;
        double d2 = (double) value2;
        return d1 == d2 ? 0 : (d1 > d2 ? 1 : -1);
      } else if (isArray(value1) || isObject(value1) || isString(value1)) {
        return value1.equals(value2) ? 0 : -1;
      } else {
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getClass().getName()));
      }
    } else {
      return -1;
    }
  }

  @Override
  public Object getProperty(Object value, String name) {
    if (value instanceof Map) {
      return ((Map<String, Object>) value).get(name);
    } else {
      return null;
    }
  }

  @Override
  public Object createNull() {
    return null;
  }

  @Override
  public Object createArray(List<Object> elements) {
    return elements;
  }

  @Override
  public Object createArray(List<Object> elements, boolean compact) {
    List<Object> compacted = new ArrayList<>(elements.size());
    for (Object element : elements) {
      if (element != null) {
        compacted.add(element);
      }
    }
    return compacted;
  }

  @Override
  public Object createString(String str) {
    return str;
  }

  @Override
  public Object createBoolean(boolean b) {
    return b;
  }

  @Override
  public Object createObject(Map<String, Object> obj) {
    return obj;
  }

  @Override
  public Object createNumber(double d) {
    return d;
  }

  @Override
  public Object callFunction(String name, List<ExpressionOrValue<Object>> arguments) {
    return functionRegistry.callFunction(this, name, arguments);
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof JcfAdapter;
  }
}
