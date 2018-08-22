package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class ReplaceFunction extends RegularExpressionFunction {
  public ReplaceFunction() {
    super(ArgumentConstraints.listOf(3, 4, ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return runtime.createString(getPattern(runtime, arguments)
        .matcher(getInputString(runtime, arguments))
        .replaceAll(getStringParam(runtime, arguments, 2)));
  }

  @Override
  protected int flagArgumentPosition() {
    return 3;
  }
}
