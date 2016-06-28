package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

@Function(arity = 1)
public class ReverseFunction extends JmesPathFunction {
  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> argument = arguments.get(0);
    if (argument.isExpression()) {
      throw new ArgumentTypeException(name(), "array or string", "expression");
    } else {
      T subject = argument.value();
      JmesPathType subjectType = adapter.typeOf(subject);
      if (subjectType == JmesPathType.ARRAY) {
        List<T> elements = new ArrayList(adapter.toList(subject));
        Collections.reverse(elements);
        return adapter.createArray(elements);
      } else if (subjectType == JmesPathType.STRING) {
        return adapter.createString(new StringBuilder(adapter.toString(subject)).reverse().toString());
      } else {
        throw new ArgumentTypeException(name(), "array or string", subjectType.toString());
      }
    }
  }
}
