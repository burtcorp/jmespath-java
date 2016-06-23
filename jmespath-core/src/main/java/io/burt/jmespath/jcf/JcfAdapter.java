package io.burt.jmespath.jcf;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.ValueType;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

import static io.burt.jmespath.ValueType.*;

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
    if (isArray(value)) {
      return (List<Object>) value;
    } else if (isObject(value)) {
      Map<String, Object> object = (Map<String, Object>) value;
      return new ArrayList(object.values());
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public String toString(Object str) {
    if (isString(str)) {
      return (String) str;
    } else {
      return unparse(str);
    }
  }

  private String unparse(Object obj) {
    if (isNumber(obj) || isBoolean(obj) || isNull(obj)) {
      return obj.toString();
    } else if (isString(obj)) {
      return String.format("\"%s\"", obj);
    } else if (isObject(obj)) {
      Map<String, Object> object = (Map<String, Object>) obj;
      StringBuilder str = new StringBuilder("{");
      if (!object.isEmpty()) {
        for (String key : object.keySet()) {
          str.append("\"").append(key).append("\"");
          str.append(":").append(unparse(object.get(key)));
          str.append(",");
        }
        str.delete(str.length() - 2, str.length());
      }
      str.append("}");
      return str.toString();
    } else if (isArray(obj)) {
      List<Object> array = (List<Object>) obj;
      StringBuilder str = new StringBuilder("[");
      if (!array.isEmpty()) {
        Object firstElement = array.get(0);
        for (Object element : array) {
          str.append(unparse(element));
          if (element != firstElement) {
            str.append(",");
          }
        }
      }
      str.append("]");
      return str.toString();
    }
    throw new IllegalStateException();
  }

  @Override
  public Number toNumber(Object n) {
    if (isNumber(n)) {
      return (Number) n;
    } else {
      return null;
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

  @Override
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
  public ValueType typeOf(Object value) {
    if (isNull(value)) {
      return NULL;
    } else if (isBoolean(value)) {
      return BOOLEAN;
    } else if (isNumber(value)) {
      return NUMBER;
    } else if (isArray(value)) {
      return ARRAY;
    } else if (isObject(value)) {
      return OBJECT;
    } else if (isString(value)) {
      return STRING;
    } else {
      throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  @Override
  public int compare(Object value1, Object value2) {
    ValueType type1 = typeOf(value1);
    ValueType type2 = typeOf(value2);
    if (type1 == type2) {
      switch (type1) {
        case NULL:
          return 0;
        case BOOLEAN:
          return value1 == value2 ? 0 : -1;
        case NUMBER:
          Comparable d1 = ((Number) value1).doubleValue();
          Comparable d2 = ((Number) value2).doubleValue();
          return d1.compareTo(d2);
        case STRING:
          String s1 = (String) value1;
          String s2 = (String) value2;
          return s1.compareTo(s2);
        case ARRAY:
        case OBJECT:
          return value1.equals(value2) ? 0 : -1;
        default:
          throw new IllegalStateException(String.format("Unknown node type encountered: %s", value1.getClass().getName()));
      }
    } else {
      return -1;
    }
  }

  @Override
  public Object getProperty(Object value, String name) {
    if (isObject(value)) {
      return ((Map<String, Object>) value).get(name);
    } else {
      return null;
    }
  }

  @Override
  public Collection<String> getPropertyNames(Object value) {
    if (isObject(value)) {
      return ((Map<String, Object>) value).keySet();
    } else {
      return Collections.emptyList();
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
  public Object createNumber(double n) {
    return n;
  }

  @Override
  public Object createNumber(long n) {
    return n;
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
