package io.burt.jmespath.function;

import java.util.Iterator;

import io.burt.jmespath.Adapter;

public interface ArgumentConstraint {
  public <T> void check(Adapter<T> adapter, Iterator<ExpressionOrValue<T>> arguments);

  public int minArity();

  public int maxArity();

  public String expectedType();
}
