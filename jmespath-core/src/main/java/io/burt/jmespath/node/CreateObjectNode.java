package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Iterator;

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

  public CreateObjectNode(Adapter<T> runtime, List<Entry<T>> entries, Node<T> source) {
    super(runtime, source);
    this.entries = entries;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new CreateObjectNode<T>(runtime, entries, source);
  }

  @Override
  public T searchWithCurrentValue(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
      return currentValue;
    } else {
      Map<T, T> object = new LinkedHashMap<>();
      for (Entry<T> entry : entries()) {
        object.put(runtime.createString(entry.key()), entry.value().search(currentValue));
      }
      return runtime.createObject(object);
    }
  }

  protected List<Entry<T>> entries() {
    return entries;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder("{");
    Iterator<Entry<T>> entryIterator = entries.iterator();
    while (entryIterator.hasNext()) {
      Entry<T> entry = entryIterator.next();
      str.append(entry.key()).append("=").append(entry.value());
      if (entryIterator.hasNext()) {
        str.append(", ");
      }
    }
    str.append("}");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    CreateObjectNode other = (CreateObjectNode) o;
    return entries().equals(other.entries());
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
