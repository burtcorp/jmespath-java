package io.burt.jmespath.jcf;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;

import io.burt.jmespath.BaseRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.util.StringEscapeHelper;

import static io.burt.jmespath.JmesPathType.*;

public class JcfRuntime extends BaseRuntime<Object> {

  private static final StringEscapeHelper jsonEscapeHelper = new StringEscapeHelper(
    true,
    'b', '\b',
    't', '\t',
    'n', '\n',
    'f', '\f',
    'r', '\r',
    '\\', '\\',
    '\"', '\"'
  );

  public JcfRuntime() {
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
        return (Boolean) value;
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

  /**
   * Helper method to render a value as JSON.
   *
   * Assumes that <code>null</code>, <code>number</code> and <code>boolean</code>
   * render themselves correctly with <code>toString</code>, and that
   * <code>string</code> renders itself as an unquoted string.
   */
  private String unparse(Object object) {
    switch (typeOf(object)) {
      case NUMBER:
      case BOOLEAN:
        return object.toString();
      case NULL:
        return "null";
      case STRING:
        return '"' + jsonEscapeHelper.escape(toString(object)) + '"';
      case OBJECT:
        return unparseObject(object);
      case ARRAY:
        return unparseArray(object);
      default:
        throw new IllegalStateException();
    }
  }

  private String unparseObject(Object object) {
    StringBuilder str = new StringBuilder("{");
    Collection<Object> propertyNames = getPropertyNames(object);
    for (Object key: propertyNames) {
      Object value = getProperty(object, key);
      str.append('"').append(jsonEscapeHelper.escape(toString(key))).append("\":");
      str.append(unparse(value));
      str.append(',');
    }
    if (!propertyNames.isEmpty()) {
      str.setLength(str.length() - 1);
    }
    return str.append('}').toString();
  }

  private String unparseArray(Object array) {
    StringBuilder str = new StringBuilder("[");
    List<Object> elements = toList(array);
    for (Object element : elements) {
      str.append(unparse(element)).append(',');
    }
    if (!elements.isEmpty()) {
      str.setLength(str.length() - 1);

    }
    return str.append(']').toString();
  }
}
