package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;

public class SequenceNode<T> extends Node<T> {
  private final List<Node<T>> nodes;

  public SequenceNode(Adapter<T> runtime, List<Node<T>> nodes) {
    super(runtime);
    this.nodes = nodes;
  }

  @Override
  protected String internalToString() {
    if (nodes.isEmpty()) {
      return null;
    } else {
      StringBuilder buffer = new StringBuilder();
      for (Node<T> node : nodes) {
        buffer.append(node).append(", ");
      }
      buffer.setLength(buffer.length() - 2);
      return buffer.toString();
    }
  }

  @Override
  protected boolean internalEquals(Object o) {
    SequenceNode<?> other = (SequenceNode<?>) o;
    return nodes.equals(other.nodes);
  }

  @Override
  protected int internalHashCode() {
    return nodes.hashCode();
  }

  @Override
  public T search(T input) {
    T value = input;
    for (Node<T> node : nodes) {
      value = node.search(value);
    }
    return value;
  }
}
