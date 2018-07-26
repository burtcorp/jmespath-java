package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ConcatFunction extends BaseFunction {
  public ConcatFunction() {
    super(ArgumentConstraints.listOf(2, ArgumentConstraints.anyValue()));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    StringBuffer sb = new StringBuffer();
    Iterator<FunctionArgument<T>> args = arguments.iterator();
    while (args.hasNext()) {
      T value = args.next().value();
      if (runtime.typeOf(value) != JmesPathType.NULL) {
        sb.append(runtime.toString(value));
      }
    }
    return runtime.createString(sb.toString());
  }
}
