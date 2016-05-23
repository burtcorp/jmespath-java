package io.burt.jmespath.ast;

public class PropertyProjectionNode extends JmesPathNode {
  private final String propertyName;

  public PropertyProjectionNode(String propertyName, JmesPathNode source) {
    super(source);
    this.propertyName = propertyName;
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
