package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 2)
public class StartsWithFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression() || secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "string", "expression");
    } else {
      T subject = firstArgument.value();
      T prefix = secondArgument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      JmesPathType prefixType = adapter.typeOf(prefix);
      if (subjectType == JmesPathType.STRING && prefixType == JmesPathType.STRING) {
        return adapter.createBoolean(adapter.toString(subject).startsWith(adapter.toString(prefix)));
      } else if (subjectType != JmesPathType.STRING) {
        throw new ArgumentTypeException(name(), "string", subjectType.toString());
      } else {
        throw new ArgumentTypeException(name(), "string", prefixType.toString());
      }
    }
  }
}
