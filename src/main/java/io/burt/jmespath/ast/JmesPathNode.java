package io.burt.jmespath.ast;

public abstract class JmesPathNode {
  private final JmesPathNode source;

  public JmesPathNode() {
    this(CurrentNode.instance);
  }

  public JmesPathNode(JmesPathNode source) {
    this.source = source;
  }

  protected JmesPathNode source() {
    return source;
  }

  @Override
  public String toString() {
    String extraArgs = internalToString();
    StringBuilder str = new StringBuilder();
    String name = getClass().getName();
    str.append(name.substring(name.lastIndexOf(".") + 1));
    str.delete(str.length() - 4, str.length());
    str.append("(");
    if (extraArgs != null) {
      str.append(extraArgs);
      if (extraArgs.length() > 0 && !extraArgs.endsWith(", ")) {
        str.append(", ");
      }
    }
    str.append(source);
    str.append(")");
    return str.toString();
  }

  protected String internalToString() {
    return null;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!o.getClass().isAssignableFrom(this.getClass())) {
      return false;
    }
    JmesPathNode other = (JmesPathNode) o;
    return internalEquals(o) && (source() == other.source() || source().equals(other.source()));
  }

  abstract protected boolean internalEquals(Object o);

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + internalHashCode();
    h = h * 31 + source.hashCode();
    return h;
  }

  abstract protected int internalHashCode();
}
