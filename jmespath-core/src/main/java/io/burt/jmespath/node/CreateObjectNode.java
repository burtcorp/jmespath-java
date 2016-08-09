package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class CreateObjectNode<T> extends JmesPathNode<T> {
  private final Entry<T>[] entries;

  public static class Entry<U> {
    private final String key;
    private final JmesPathNode<U> value;

    public Entry(String key, JmesPathNode<U> value) {
      this.key = key;
      this.value = value;
    }

    protected String key() {
      return key;
    }

    protected JmesPathNode<U> value() {
      return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Entry)) {
        return false;
      }
      Entry<U> other = (Entry<U>) o;
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

  public CreateObjectNode(Adapter<T> runtime, Entry<T>[] entries, JmesPathNode<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  @Override
  public T evaluateOne(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      Map<T, T> object = new LinkedHashMap<>();
      for (Entry<T> entry : entries()) {
        object.put(runtime.createString(entry.key()), entry.value().evaluate(currentValue));
      }
      return runtime.createObject(object);
    }
  }

  protected Entry<T>[] entries() {
    return entries;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("{");
    for (Entry<T> entry : entries) {
      str.append(entry.key()).append("=").append(entry.value()).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("}");
    return str.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    CreateObjectNode<T> other = (CreateObjectNode<T>) o;
    return Arrays.equals(entries(), other.entries());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    for (Entry<T> entry : entries) {
      h = h * 31 + entry.hashCode();
    }
    return h;
  }
}
