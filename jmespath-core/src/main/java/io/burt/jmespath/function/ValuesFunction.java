package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ValuesFunction extends JmesPathFunction {
  public ValuesFunction() {
    super(1, 1);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "object", "expression");
    } else {
      T subject = argument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      if (subjectType == JmesPathType.OBJECT) {
        return adapter.createArray(adapter.toList(subject));
      } else {
        throw new ArgumentTypeException(name(), "object", subjectType.toString());
      }
    }
  }
}
