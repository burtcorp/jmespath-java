package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 1)
public class LengthFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "string, array or object", "expression");
    } else {
      T subject = argument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      if (subjectType == JmesPathType.STRING) {
        return adapter.createNumber((long) adapter.toString(subject).length());
      } else if (subjectType == JmesPathType.ARRAY || subjectType == JmesPathType.OBJECT) {
        return adapter.createNumber((long) adapter.toList(subject).size());
      } else {
        throw new ArgumentTypeException(name(), "string, array or object", subjectType.toString());
      }
    }
  }
}
