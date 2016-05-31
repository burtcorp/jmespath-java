package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public abstract class JmesPathFunction {
  private final int minArity;
  private final int maxArity;
  private final String name;

  public JmesPathFunction(int minArity, int maxArity) {
    this.minArity = minArity;
    this.maxArity = maxArity;
    String n = getClass().getName();
    this.name = n.substring(n.lastIndexOf(".") + 1).replace("Function", "").toLowerCase();
  }

  public String name() {
    return name;
  }

  public <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    int numArguments = arguments.size();
    if (numArguments >= minArity && numArguments <= maxArity) {
      return internalCall(adapter, arguments);
    } else {
      String message;
      if (maxArity == minArity) {
        message = String.format("Wrong number of arguments calling %s: expected %d but was %d", name(), minArity, numArguments);
      } else if (arguments.size() < minArity) {
        message = String.format("Wrong number of arguments calling %s: expected at least %d but was %d", name(), minArity, numArguments);
      } else {
        message = String.format("Wrong number of arguments calling %s: expected at most %d but was %d", name(), maxArity, numArguments);
      }
      throw new FunctionCallException(message);
    }
  }

  protected abstract <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments);
}
