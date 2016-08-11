package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.function.ValueOrExpression;

public class FunctionCallNode<T> extends Node<T> {
  private final Function implementation;
  private final List<? extends Node<T>> args;

  public FunctionCallNode(Adapter<T> runtime, Function implementation, List<? extends Node<T>> args, Node<T> source) {
    super(runtime, source);
    this.implementation = implementation;
    this.args = args;
  }

  @Override
  protected T searchOne(T currentValue) {
    List<ValueOrExpression<T>> arguments = new ArrayList<>(args.size());
    for (Node<T> arg : args()) {
      if (arg instanceof ExpressionReferenceNode) {
        arguments.add(ValueOrExpression.of(arg));
      } else {
        arguments.add(ValueOrExpression.of(arg.search(currentValue)));
      }
    }
    return implementation.call(runtime, arguments);
  }

  protected Function implementation() {
    return implementation;
  }

  protected List<? extends Node<T>> args() {
    return args;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder(implementation().name()).append(", [");
    Iterator<? extends Node<T>> argIterator = args.iterator();
    while (argIterator.hasNext()) {
      Node<T> arg = argIterator.next();
      str.append(arg);
      if (argIterator.hasNext()) {
        str.append(", ");
      }
    }
    str.append("]");
    return str.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    FunctionCallNode<T> other = (FunctionCallNode<T>) o;
    return implementation.name().equals(other.implementation().name()) && args().equals(other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + implementation().name().hashCode();
    for (Node<T> node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
