package io.burt.jmespath.function;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class KeysFunction extends JmesPathFunction {
  public KeysFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.OBJECT));
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    return adapter.createArray(adapter.getPropertyNames(arguments.get(0).value()));
  }
}
