package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ReverseFunction extends BaseFunction {
  public ReverseFunction() {
    super(ArgumentConstraints.typeOf(JmesPathType.ARRAY, JmesPathType.STRING));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
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
