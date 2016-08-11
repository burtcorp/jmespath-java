package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.function.FunctionArgument;

public class FunctionCallNode<T> extends Node<T> {
  private final Function implementation;
  private final List<? extends Expression<T>> args;

  public FunctionCallNode(Adapter<T> runtime, Function implementation, List<? extends Expression<T>> args, Node<T> source) {
    super(runtime, source);
    this.implementation = implementation;
    this.args = args;
  }

  @Override
  protected T searchOne(T currentValue) {
    List<FunctionArgument<T>> arguments = new ArrayList<>(args.size());
    for (Expression<T> arg : args()) {
      if (arg instanceof ExpressionReferenceNode) {
        arguments.add(FunctionArgument.of(arg));
      } else {
        arguments.add(FunctionArgument.of(arg.search(currentValue)));
      }
    }
    return implementation.call(runtime, arguments);
  }

  protected Function implementation() {
    return implementation;
  }

  protected List<? extends Expression<T>> args() {
    return args;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder(implementation().name()).append(", [");
    Iterator<? extends Expression<T>> argIterator = args.iterator();
    while (argIterator.hasNext()) {
      Expression<T> arg = argIterator.next();
      str.append(arg);
      if (argIterator.hasNext()) {
        str.append(", ");
      }
    }
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    FunctionCallNode other = (FunctionCallNode) o;
    return implementation.name().equals(other.implementation().name()) && args().equals(other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + implementation().name().hashCode();
    for (Expression<T> node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
