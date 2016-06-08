package io.burt.jmespath.function;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;

public class KeysFunction extends JmesPathFunction {
  public KeysFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    if (adapter.isObject(subject)) {
      Collection<String> propertyNames = adapter.getPropertyNames(subject);
      List<T> names = new ArrayList<>(propertyNames.size());
      for (String name : propertyNames) {
        names.add(adapter.createString(name));
      }
      return adapter.createArray(names);
    } else {
      throw new ArgumentTypeException(name(), "object", adapter.typeOf(subject));
    }
  }
}
