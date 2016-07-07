package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ContainsFunction extends JmesPathFunction {
  public ContainsFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.ARRAY, JmesPathType.STRING),
      ArgumentConstraints.anyValue()
    );
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T haystack = arguments.get(0).value();
    T needle = arguments.get(1).value();
    JmesPathType haystackType = adapter.typeOf(haystack);
    if (haystackType == JmesPathType.ARRAY) {
      return adapter.createBoolean(adapter.toList(haystack).contains(needle));
    } else {
      return adapter.createBoolean(adapter.toString(haystack).indexOf(adapter.toString(needle)) >= 0);
    }
  }
}
