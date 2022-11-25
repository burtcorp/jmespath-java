package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;

public class NumberNode<T> extends Node<T> {
  private final Number rawNumber;
  
  public NumberNode(Adapter<T> runtime, Number rawNumber) {
    super(runtime);
    this.rawNumber = rawNumber;
  }
  
  @Override
  public T search(T input) {
    if (rawNumber instanceof Long) {
      return runtime.createNumber((Long) rawNumber);
    } else {
      return runtime.createNumber((Double) rawNumber);
    }
  }
  
  @Override
  protected String internalToString() {
    return String.valueOf(rawNumber);
  }
  
  @Override
  protected boolean internalEquals(Object o) {
    NumberNode<?> other = (NumberNode<?>) o;
    return rawNumber.equals(other.rawNumber);
  }
  
  @Override
  protected int internalHashCode() {
    return rawNumber.hashCode();
  }
}
