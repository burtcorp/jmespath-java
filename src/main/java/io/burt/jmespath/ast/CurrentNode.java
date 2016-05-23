package io.burt.jmespath.ast;

public class CurrentNode extends JmesPathNode {
  public static final JmesPathNode instance = new CurrentNode();

  private CurrentNode() {
    super(null);
  }

  @Override
  public String toString() {
    return "Current()";
  }

  @Override
  public boolean equals(Object o) {
    return internalEquals(o);
  }

  @Override
  public boolean internalEquals(Object o) {
    return (this == o);
  }

  @Override
  public int hashCode() {
    return internalHashCode();
  }

  @Override
  protected int internalHashCode() {
    return 17;
  }
}
