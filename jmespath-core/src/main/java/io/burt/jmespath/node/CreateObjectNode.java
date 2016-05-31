package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

import io.burt.jmespath.Adapter;

public class CreateObjectNode extends JmesPathNode {
  private final Entry[] entries;

  public static class Entry {
    private final String key;
    private final JmesPathNode value;

    public Entry(String key, JmesPathNode value) {
      this.key = key;
      this.value = value;
    }

    protected String key() {
      return key;
    }

    protected JmesPathNode value() {
      return value;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry other = (Entry) o;
      return key().equals(other.key()) && value().equals(other.value());
    }

    @Override
    public int hashCode() {
      int h = 1;
      h = h * 31 + key.hashCode();
      h = h * 31 + value.hashCode();
      return h;
    }
  }

  public CreateObjectNode(Entry[] entries, JmesPathNode source) {
    super(source);
    this.entries = entries;
  }

  @Override
  public <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    if (adapter.isNull(currentValue)) {
      return currentValue;
    } else {
      Map<String, T> object = new LinkedHashMap<>();
      for (Entry entry : entries()) {
        object.put(entry.key(), entry.value().evaluate(adapter, currentValue));
      }
      return adapter.createObject(object);
    }
  }

  protected Entry[] entries() {
    return entries;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("{");
    for (Entry entry : entries) {
      str.append(entry.key()).append("=").append(entry.value()).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("}");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateObjectNode other = (CreateObjectNode) o;
    return Arrays.equals(entries(), other.entries());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (Entry entry : entries) {
      h = h * 31 + entry.hashCode();
    }
    return h;
  }
}
