package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public abstract class JmesPathFunction {
  private final String name;

  public JmesPathFunction() {
    String n = getClass().getName();
    this.name = n.substring(n.lastIndexOf(".") + 1).replace("Function", "").toLowerCase();
  }

  public String name() {
    return name;
  }

  public abstract <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments);
}
