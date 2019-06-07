package io.burt.jmespath.node;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.function.FunctionArgument;

public class FunctionCallNode<T> extends Node<T> {
  private final Function implementation;
  private final List<Expression<T>> args;

  public FunctionCallNode(Adapter<T> runtime, Function implementation, List<? extends Expression<T>> args) {
    super(runtime);
    this.implementation = implementation;
    this.args = new ArrayList<>(args);
  }

  @Override
  public T search(T input) {
    List<FunctionArgument<T>> arguments = new ArrayList<>(args.size());
    for (Expression<T> arg : args) {
      if (arg instanceof ExpressionReferenceNode) {
        arguments.add(FunctionArgument.of(arg));
      } else {
        arguments.add(FunctionArgument.of(arg.search(input)));
      }
    }
    return implementation.call(runtime, arguments);
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder();
    if (implementation != null) {
      str.append(implementation.name());
    }
    str.append(", [");
    for (Expression<T> arg : args) {
      str.append(arg).append(", ");
    }
    if (!args.isEmpty()) {
      str.setLength(str.length() - 2);
    }
    return str.append(']').toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    FunctionCallNode<?> other = (FunctionCallNode<?>) o;
    return implementation.name().equals(other.implementation.name()) && args.equals(other.args);
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + implementation.name().hashCode();
    for (Expression<T> node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
