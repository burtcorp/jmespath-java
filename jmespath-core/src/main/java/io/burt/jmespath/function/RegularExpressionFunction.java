package io.burt.jmespath.function;

import java.util.List;
import java.util.regex.Pattern;

import io.burt.jmespath.Adapter;

public abstract class RegularExpressionFunction extends BaseFunction {
  public RegularExpressionFunction(ArgumentConstraint argumentConstraints) {
    super(argumentConstraints);
  }

  protected <T> String getInputString(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    return getStringParam(runtime, arguments, inputArgumentPosition());
  }

  protected <T> Pattern getPattern(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    String regex = getStringParam(runtime, arguments, patternArgumentPosition());
    return Pattern.compile(regex, getFlags(runtime, arguments));
  }

  protected <T> int getFlags(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    if (arguments.size() <= flagArgumentPosition())
      return 0;
    return convertPatternFlags(getStringParam(runtime, arguments, flagArgumentPosition()));
  }

  protected <T> String getStringParam(Adapter<T> runtime, List<FunctionArgument<T>> arguments, int i) {
    return runtime.toString(arguments.get(i).value());
  }

  /**
   * Subclasses may override these methods if parameter positions are different than usual.
   */
  protected int inputArgumentPosition() {
    return 0;
  }

  protected int patternArgumentPosition() {
    return 1;
  }

  protected int flagArgumentPosition() {
    return 2;
  }

  private int convertPatternFlags(String flagStr) {
    int flags = 0;
    for (int i = 0; i < flagStr.length(); ++i) {
      switch (flagStr.charAt(i)) {
        case 's':
          flags |= Pattern.DOTALL;
          break;
        case 'm':
          flags |= Pattern.MULTILINE;
          break;
        case 'i':
          flags |= Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
          break;
        case 'x':
          flags |= Pattern.COMMENTS;
          break;
        case 'q':
          flags |= Pattern.LITERAL;
        default:
          break;

      }
    }
    return flags;
  }
}
