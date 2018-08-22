package io.burt.jmespath.function;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class TokenizeFunction extends RegularExpressionFunction {
  public TokenizeFunction() {
    super(ArgumentConstraints.listOf(1, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    List<T> result = new LinkedList<>();
    Pattern pattern = (arguments.size() > 1) ? getPattern(runtime, arguments) : Pattern.compile("\\s+");
    for (String parts: pattern.split(getInputString(runtime, arguments), -1)) {
      /*
       * if arguments.size == 1 then leading and trailing empty parts has to be discarded
       * but the pattern above ensures that nowhere else such an empty part may occur.
       */
      if (1 < arguments.size() || !isEmpty(parts)) {
        result.add(runtime.createString(parts));
      }
    }
    return runtime.createArray(result);
  }
}
