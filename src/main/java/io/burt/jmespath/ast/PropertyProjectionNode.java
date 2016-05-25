package io.burt.jmespath.ast;

import io.burt.jmespath.Adapter;

public class PropertyProjectionNode extends ProjectionNode {
  private final String propertyName;

  public PropertyProjectionNode(String propertyName, JmesPathNode source) {
    super(source);
    this.propertyName = propertyName;
  }

  @Override
  public <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    return adapter.getProperty(currentValue, propertyName());
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
    PropertyProjectionNode other = (PropertyProjectionNode) o;
    return propertyName().equals(other.propertyName());
  }

  @Override
  protected int internalHashCode() {
    return propertyName().hashCode();
  }
}
