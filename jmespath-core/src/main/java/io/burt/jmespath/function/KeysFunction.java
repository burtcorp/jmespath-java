package io.burt.jmespath.function;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 1)
public class KeysFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "object", "expression");
    } else {
      T subject = argument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      if (subjectType == JmesPathType.OBJECT) {
        return adapter.createArray(adapter.getPropertyNames(subject));
      } else {
        throw new ArgumentTypeException(name(), "object", subjectType.toString());
      }
    }
  }
}
