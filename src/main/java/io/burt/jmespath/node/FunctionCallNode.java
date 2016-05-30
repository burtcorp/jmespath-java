package io.burt.jmespath.node;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.FunctionCallException;
import io.burt.jmespath.function.ExpressionOrValue;

public class FunctionCallNode extends JmesPathNode {
  private final String name;
  private final JmesPathNode[] args;

  public FunctionCallNode(String name, JmesPathNode[] args, JmesPathNode source) {
    super(source);
    this.name = name;
    this.args = args;
  }

  @Override
  protected <T> T evaluateOne(Adapter<T> adapter, T currentValue) {
    List<ExpressionOrValue<T>> arguments = new ArrayList<>(args.length);
    for (JmesPathNode arg : args()) {
      if (arg instanceof ExpressionReferenceNode) {
        arguments.add(new ExpressionOrValue<T>(arg));
      } else {
        arguments.add(new ExpressionOrValue<T>(arg.evaluate(adapter, currentValue)));
      }
    }
    return adapter.callFunction(name(), arguments);
  }

  protected String name() {
    return name;
  }

  protected JmesPathNode[] args() {
    return args;
  }

  @Override
  protected String internalToString() {
    StringBuilder str = new StringBuilder(name).append(", [");
    for (JmesPathNode node : args) {
      str.append(node).append(", ");
    }
    str.delete(str.length() - 2, str.length());
    str.append("]");
    return str.toString();
  }

  @Override
  protected boolean internalEquals(Object o) {
    FunctionCallNode other = (FunctionCallNode) o;
    return name().equals(other.name()) && Arrays.equals(args(), other.args());
  }

  @Override
  protected int internalHashCode() {
    int h = 1;
    h = h * 31 + name.hashCode();
    for (JmesPathNode node : args) {
      h = h * 31 + node.hashCode();
    }
    return h;
  }
}
