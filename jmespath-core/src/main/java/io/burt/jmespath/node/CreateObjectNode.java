package io.burt.jmespath.node;

import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class CreateObjectNode<T> extends Node<T> {
  private final List<Entry<T>> entries;

  public static class Entry<U> {
    private final String key;
    private final Expression<U> value;

    public Entry(String key, Expression<U> value) {
      this.key = key;
      this.value = value;
    }

    protected String key() {
      return key;
    }

    protected Expression<U> value() {
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
      Entry<?> other = (Entry<?>) o;
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

  public CreateObjectNode(Adapter<T> runtime, List<Entry<T>> entries) {
    super(runtime);
    this.entries = entries;
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.NULL) {
      return input;
    } else {
      Map<T, T> object = new LinkedHashMap<>();
      for (Entry<T> entry : entries) {
        object.put(runtime.createString(entry.key()), entry.value().search(input));
      }
      return runtime.createObject(object);
    }
  }

  @Override
  protected String internalToString() {
    if (entries.isEmpty()) {
      return "{}";
    }

    StringBuilder str = new StringBuilder("{");
    for (Entry<T> entry : entries) {
      str.append(entry.key()).append('=').append(entry.value()).append(", ");
    }
    str.setLength(str.length() - 2);
    return str.append('}').toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateObjectNode<?> other = (CreateObjectNode<?>) o;
    return entries.equals(other.entries);
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
