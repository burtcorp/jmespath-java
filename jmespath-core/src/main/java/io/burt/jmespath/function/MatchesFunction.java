package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class MatchesFunction extends RegularExpressionFunction {

  public MatchesFunction() {
    super(ArgumentConstraints.listOf(2, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return runtime.createBoolean(getPattern(runtime, arguments).matcher(getInputString(runtime, arguments)).find());
  }
}
