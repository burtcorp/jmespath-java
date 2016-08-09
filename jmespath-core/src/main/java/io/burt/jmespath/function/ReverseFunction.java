package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class ReverseFunction extends JmesPathFunction {
  public ReverseFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.ARRAY, JmesPathType.STRING));
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T subject = arguments.get(0).value();
    JmesPathType subjectType = runtime.typeOf(subject);
    if (subjectType == JmesPathType.ARRAY) {
      List<T> elements = new ArrayList<T>(runtime.toList(subject));
      Collections.reverse(elements);
      return runtime.createArray(elements);
    } else {
      return runtime.createString(new StringBuilder(runtime.toString(subject)).reverse().toString());
    }
  }
}
