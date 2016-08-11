package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ContainsFunction extends Function {
  public ContainsFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.ARRAY, JmesPathType.STRING),
      ArgumentConstraints.anyValue()
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T haystack = arguments.get(0).value();
    T needle = arguments.get(1).value();
    JmesPathType haystackType = runtime.typeOf(haystack);
    if (haystackType == JmesPathType.ARRAY) {
      return runtime.createBoolean(runtime.toList(haystack).contains(needle));
    } else {
      return runtime.createBoolean(runtime.toString(haystack).indexOf(runtime.toString(needle)) >= 0);
    }
  }
}
