package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public class NegateNode<T> extends Node<T> {
  private final Expression<T> negated;

  public NegateNode(Adapter<T> runtime, Expression<T> negated) {
    super(runtime);
    this.negated = negated;
  }

  public Expression<T> negated() {
    return negated;
  }

  @Override
  public T search(T input) {
    return runtime.createBoolean(!runtime.isTruthy(negated.search(input)));
  }

  @Override
  protected boolean internalEquals(Object o) {
    NegateNode<?> other = (NegateNode<?>)o;
    return negated.equals(other.negated);
  }

  @Override
  protected int internalHashCode() {
    return 17 + 31 * negated.hashCode();
  }
}
