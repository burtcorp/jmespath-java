package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.ExpressionOrValue;

public class FunctionCallNode<T> extends JmesPathNode<T> {
  private final String name;
  private final JmesPathNode<T>[] args;

  public FunctionCallNode(Adapter<T> runtime, String name, JmesPathNode<T>[] args, JmesPathNode<T> source) {
    super(runtime, source);
    this.name = name;
    this.args = args;
  }

  @Override
  protected T searchOne(T currentValue) {
    List<ExpressionOrValue<T>> arguments = new ArrayList<>(args.length);
    for (JmesPathNode<T> arg : args()) {
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

  protected JmesPathNode<T>[] args() {
    return args;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder(name).append(", [");
    for (JmesPathNode<T> node : args) {
      str.append(node).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected boolean internalEquals(Object o) {
    FunctionCallNode<T> other = (FunctionCallNode<T>) o;
    return name().equals(other.name()) && Arrays.equals(args(), other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + name.hashCode();
    for (JmesPathNode<T> node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
