package io.burt.jmespath.node;

import java.util.List;
import java.util.LinkedList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.Expression;

public abstract class JmesPathNode<T> implements Expression<T> {
  protected final Adapter<T> runtime;
  private final JmesPathNode<T> source;

  public JmesPathNode(Adapter<T> runtime) {
    this(runtime, new CurrentNode<T>(runtime));
  }

  public JmesPathNode(Adapter<T> runtime, JmesPathNode<T> source) {
    this.runtime = runtime;
    this.source = source;
  }

  protected boolean isProjection() {
    return source().isProjection();
  }

  public T search(T input) {
    return searchWithCurrentValue(source().search(input));
  }

  protected T searchWithCurrentValue(T currentValue) {
    if (isProjection()) {
      if (runtime.typeOf(currentValue) == JmesPathType.NULL) {
        return currentValue;
      } else {
        List<T> outputs = new LinkedList<>();
        for (T projectionElement : runtime.toList(currentValue)) {
          T value = searchOne(projectionElement);
          if (runtime.typeOf(value) != JmesPathType.NULL) {
            outputs.add(value);
          }
        }
        return runtime.createArray(outputs);
      }
    } else {
      return searchOne(currentValue);
    }
  }

  protected T searchOne(T currentValue) {
    return currentValue;
  }

  protected JmesPathNode<T> source() {
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
    str.append(source());
    str.append(")");
    return str.toString();
  }

  protected String internalToString() {
    return null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!getClass().isInstance(o)) {
      return false;
    }
    JmesPathNode<T> other = (JmesPathNode<T>) o;
    return internalEquals(o) && (source() == other.source() || (source() != null && other.source() != null && source().equals(other.source())));
  }

  abstract protected boolean internalEquals(Object o);

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + internalHashCode();
    h = h * 31 + (source() == null ? 0 : source().hashCode());
    return h;
  }

  abstract protected int internalHashCode();
}
