package io.burt.jmespath.function;

import java.util.LinkedList;
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class TokenizeFunction extends RegularExpressionFunction {
  public TokenizeFunction() {
    super(ArgumentConstraints.listOf(2, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    List<T> result = new LinkedList<>();
    for (String parts: getPattern(runtime, arguments).split(getInputString(runtime, arguments))) {
      result.add(runtime.createString(parts));
    }
    return runtime.createArray(result);
  }
}
