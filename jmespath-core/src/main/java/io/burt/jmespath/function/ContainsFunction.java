package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.node.JmesPathNode;

public class ContainsFunction extends JmesPathFunction {
  public ContainsFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T haystack = arguments.get(0).value();
    T needle = arguments.get(1).value();
    if (adapter.isArray(haystack)) {
      return adapter.createBoolean(adapter.toList(haystack).contains(needle));
    } else if (adapter.isString(haystack)) {
      return adapter.createBoolean(adapter.toString(haystack).indexOf(adapter.toString(needle)) >= 0);
    } else {
      throw new FunctionCallException(String.format("Expected array got %s", adapter.typeOf(haystack)));
    }
  }
}
