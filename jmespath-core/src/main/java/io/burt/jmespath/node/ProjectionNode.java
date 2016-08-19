package io.burt.jmespath.node;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class ProjectionNode<T> extends Node<T> {
  private final Expression<T> projection;

  public ProjectionNode(Adapter<T> runtime, Expression<T> projection, Node<T> source) {
    super(runtime, source);
    this.projection = projection;
  }

  @Override
  public Node<T> copyWithSource(Node<T> source) {
    return new ProjectionNode<>(runtime, projection, source);
  }

  @Override
  public T searchWithCurrentValue(T currentValue) {
    if (runtime.typeOf(currentValue) == JmesPathType.ARRAY) {
      List<T> inputs = runtime.toList(currentValue);
      List<T> results = new ArrayList<>(inputs.size());
      for (T input : inputs) {
        T result = projection.search(input);
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
