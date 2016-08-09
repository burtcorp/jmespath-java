package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class PropertyNode extends JmesPathNode {
  private final String propertyName;

  public PropertyNode(String propertyName, JmesPathNode source) {
    super(source);
    this.propertyName = propertyName;
  }

  @Override
  public <T> T evaluateOne(Adapter<T> runtime, T projectionElement) {
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
  protected boolean internalEquals(Object o) {
    PropertyNode other = (PropertyNode) o;
    return propertyName().equals(other.propertyName());
  }

  @Override
  protected int internalHashCode() {
    return propertyName().hashCode();
  }
}
