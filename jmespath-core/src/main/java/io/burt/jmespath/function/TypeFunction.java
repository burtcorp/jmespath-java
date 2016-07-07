package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

public class TypeFunction extends JmesPathFunction {
  public TypeFunction() {
    super(ArgumentConstraints.anyValue());
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    return adapter.createString(adapter.typeOf(arguments.get(0).value()).toString());
  }
}
