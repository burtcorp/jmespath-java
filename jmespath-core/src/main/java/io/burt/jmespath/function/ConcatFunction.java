package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ConcatFunction extends BaseFunction {
  public ConcatFunction() {
    super(ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    StringBuffer sb = new StringBuffer();
    Iterator<FunctionArgument<T>> args = arguments.iterator();
    while (args.hasNext()) {
      sb.append(runtime.toString(args.next().value()));
    }
    return runtime.createString(sb.toString());
  }
}
