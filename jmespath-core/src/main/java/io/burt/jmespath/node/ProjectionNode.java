package io.burt.jmespath.node;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class ProjectionNode<T> extends Node<T> {
  private final Expression<T> projection;

  public ProjectionNode(Adapter<T> runtime, Expression<T> projection) {
    super(runtime);
    this.projection = projection;
  }

  @Override
  public T search(T input) {
    if (runtime.typeOf(input) == JmesPathType.ARRAY) {
      List<T> inputList = runtime.toList(input);
      List<T> results = new ArrayList<>(inputList.size());
      for (T inputItem : inputList) {
        T result = projection.search(inputItem);
        JmesPathType type = runtime.typeOf(result);
        if (type != JmesPathType.NULL) {
          results.add(result);
        }
      }
      return runtime.createArray(results);
    } else {
      return runtime.createNull();
    }
  }

  @Override
  protected String internalToString() {
    return projection.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    ProjectionNode<?> other = (ProjectionNode<?>) o;
    return projection.equals(other.projection);
  }

  @Override
  protected int internalHashCode() {
    return projection.hashCode();
  }
}
