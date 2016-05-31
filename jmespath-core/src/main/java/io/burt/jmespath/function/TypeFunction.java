package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class TypeFunction extends JmesPathFunction {
  @Override
  public <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    return adapter.createString(adapter.typeOf(arguments.get(0).value()));
  }
}
