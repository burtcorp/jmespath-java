package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class PropertyNode<T> extends Node<T> {
  private final String propertyName;

  public PropertyNode(Adapter<T> runtime, String propertyName, Node<T> source) {
    super(runtime, source);
    this.propertyName = propertyName;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new PropertyNode<T>(runtime, propertyName, source);
  }

  @Override
  public T searchWithCurrentValue(T currentValue) {
    return runtime.getProperty(currentValue, propertyName());
  }

  protected String propertyName() {
    return propertyName;
  }

  @Override
  protected String internalToString() {
    return propertyName;
  }

  @Override
  protected boolean internalEquals(Object o) {
    PropertyNode other = (PropertyNode) o;
    return propertyName().equals(other.propertyName());
  }

  @Override
  protected int internalHashCode() {
    return propertyName().hashCode();
  }
}
