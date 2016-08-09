package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class PropertyNode<T> extends JmesPathNode<T> {
  private final String propertyName;

  public PropertyNode(Adapter<T> runtime, String propertyName, JmesPathNode<T> source) {
    super(runtime, source);
    this.propertyName = propertyName;
  }

  @Override
  public T evaluateOne(T projectionElement) {
    return runtime.getProperty(projectionElement, propertyName());
  }

  protected String propertyName() {
    return propertyName;
  }

  @Override
  protected String internalToString() {
    return propertyName;
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    PropertyNode<T> other = (PropertyNode<T>) o;
    return propertyName().equals(other.propertyName());
  }

  @Override
  protected int internalHashCode() {
    return propertyName().hashCode();
  }
}
