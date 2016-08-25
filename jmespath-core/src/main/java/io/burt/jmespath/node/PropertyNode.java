package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class PropertyNode<T> extends Node<T> {
  private final String rawPropertyName;
  private final T propertyName;

  public PropertyNode(Adapter<T> runtime, String rawPropertyName) {
    super(runtime);
    this.rawPropertyName = rawPropertyName;
    this.propertyName = runtime.createString(rawPropertyName);
  }

  @Override
  public T search(T input) {
    return runtime.getProperty(input, propertyName);
  }

  @Override
  protected String internalToString() {
    return rawPropertyName;
  }

  @Override
  protected boolean internalEquals(Object o) {
    PropertyNode<?> other = (PropertyNode<?>) o;
    return rawPropertyName.equals(other.rawPropertyName);
  }

  @Override
  protected int internalHashCode() {
    return rawPropertyName.hashCode();
  }
}
