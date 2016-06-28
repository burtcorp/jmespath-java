package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 2)
public class EndsWithFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression() || secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "string", "expression");
    } else {
      T subject = firstArgument.value();
      T suffix = secondArgument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      JmesPathType suffixType = adapter.typeOf(suffix);
      if (subjectType == JmesPathType.STRING && suffixType == JmesPathType.STRING) {
        return adapter.createBoolean(adapter.toString(subject).endsWith(adapter.toString(suffix)));
      } else if (subjectType != JmesPathType.STRING) {
        throw new ArgumentTypeException(name(), "string", subjectType.toString());
      } else {
        throw new ArgumentTypeException(name(), "string", suffixType.toString());
      }
    }
  }
}
