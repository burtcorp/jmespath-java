package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.ExpressionOrValue;

public class FunctionCallNode<T> extends Node<T> {
  private final String name;
  private final List<? extends Node<T>> args;

  public FunctionCallNode(Adapter<T> runtime, String name, List<? extends Node<T>> args, Node<T> source) {
    super(runtime, source);
    this.name = name;
    this.args = args;
  }

  @Override
  protected T searchOne(T currentValue) {
    List<ExpressionOrValue<T>> arguments = new ArrayList<>(args.size());
    for (Node<T> arg : args()) {
      if (arg instanceof ExpressionReferenceNode) {
        arguments.add(new ExpressionOrValue<T>(arg));
      } else {
        arguments.add(new ExpressionOrValue<T>(arg.search(currentValue)));
      }
    }
    return runtime.callFunction(name(), arguments);
  }

  protected String name() {
    return name;
  }

  protected List<? extends Node<T>> args() {
    return args;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder(name).append(", [");
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
    return name().equals(other.name()) && args().equals(other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + name.hashCode();
    for (Node<T> node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
