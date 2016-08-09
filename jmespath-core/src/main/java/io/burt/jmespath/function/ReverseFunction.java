package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ReverseFunction extends JmesPathFunction {
  public ReverseFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.ARRAY, JmesPathType.STRING));
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    JmesPathType subjectType = adapter.typeOf(subject);
    if (subjectType == JmesPathType.ARRAY) {
      List<T> elements = new ArrayList<T>(adapter.toList(subject));
      Collections.reverse(elements);
      return adapter.createArray(elements);
    } else {
      return adapter.createString(new StringBuilder(adapter.toString(subject)).reverse().toString());
    }
  }
}
