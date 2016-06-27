package io.burt.jmespath.jcf;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import io.burt.jmespath.BaseAdapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.ExpressionOrValue;

import static io.burt.jmespath.JmesPathType.*;

public class JcfAdapter extends BaseAdapter<Object> {
  @Override
  public Object parseString(String string) {
    return JsonParser.fromString(string, this);
  }

  @Override
  public List<Object> toList(Object value) {
    if (isArray(value)) {
      if (value instanceof List) {
        return (List<Object>) value;
      } else {
        return new ArrayList<Object>((Collection<Object>) value);
      }
    } else if (isObject(value)) {
      Map<Object, Object> object = (Map<Object, Object>) value;
      return new ArrayList<Object>(object.values());
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
    return value instanceof Collection;
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
  public Object getProperty(Object value, String name) {
    if (isObject(value)) {
      return ((Map<String, Object>) value).get(name);
    } else {
      return null;
    }
  }

  @Override
  public Object getProperty(Object value, Object name) {
    return getProperty(value, (String) name);
  }

  @Override
  public Collection<Object> getPropertyNames(Object value) {
    if (isObject(value)) {
      return ((Map<Object, Object>) value).keySet();
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public Object createNull() {
    return null;
  }

  @Override
  public Object createArray(Collection<Object> elements) {
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
  public Object createObject(Map<Object, Object> obj) {
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
}
