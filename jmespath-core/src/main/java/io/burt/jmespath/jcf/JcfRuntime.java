package io.burt.jmespath.jcf;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;

import static io.burt.jmespath.JmesPathType.*;

public class JcfRuntime extends BaseRuntime<Object> {
  public JcfRuntime() {
    super();
  }

  public JcfRuntime(RuntimeConfiguration configuration) {
    super(configuration);
  }

  @Override
  public Object parseString(String string) {
    return JsonParser.fromString(string, this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<Object> toList(Object value) {
    switch (typeOf(value)) {
      case ARRAY:
        if (value instanceof List) {
          return (List<Object>) value;
        } else {
          return new ArrayList<>((Collection<Object>) value);
        }
      case OBJECT:
        Map<Object, Object> object = (Map<Object, Object>) value;
        return new ArrayList<>(object.values());
      default:
        return Collections.emptyList();
    }
  }

  @Override
  public String toString(Object str) {
    if (typeOf(str) == STRING) {
      return (String) str;
    } else {
      return unparse(str);
    }
  }

  @Override
  public Number toNumber(Object n) {
    if (typeOf(n) == NUMBER) {
      return (Number) n;
    } else {
      return null;
    }
  }

  @Override
  public JmesPathType typeOf(Object value) {
    if (value == null) {
      return NULL;
    } else if (value instanceof Boolean) {
      return BOOLEAN;
    } else if (value instanceof Number) {
      return NUMBER;
    } else if (value instanceof Map) {
      return OBJECT;
    } else if (value instanceof Collection) {
      return ARRAY;
    } else if (value instanceof String) {
      return STRING;
    } else {
      throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean isTruthy(Object value) {
    switch (typeOf(value)) {
      case NULL:
        return false;
      case NUMBER:
        return true;
      case BOOLEAN:
        return ((Boolean)value).booleanValue();
      case ARRAY:
        return !((Collection<Object>) value).isEmpty();
      case OBJECT:
        return !((Map<Object,Object>) value).isEmpty();
      case STRING:
        return !((String) value).isEmpty();
      default:
        throw new IllegalStateException(String.format("Unknown node type encountered: %s", value.getClass().getName()));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Object getProperty(Object value, Object name) {
    if (typeOf(value) == OBJECT) {
      return ((Map<Object, Object>) value).get(name);
    } else {
      return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public Collection<Object> getPropertyNames(Object value) {
    if (typeOf(value) == OBJECT) {
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
    if (elements instanceof List) {
      return elements;
    } else {
      return new ArrayList<>(elements);
    }
  }

  @Override
  public Object createString(String str) {
    return str;
  }

  @Override
  public Object createBoolean(boolean b) {
    return Boolean.valueOf(b);
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
