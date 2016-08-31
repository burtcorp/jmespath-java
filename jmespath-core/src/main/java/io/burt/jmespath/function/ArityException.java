package io.burt.jmespath.function;

@SuppressWarnings("serial")
public class ArityException extends FunctionCallException {
  public ArityException(Function function, int numArguments) {
    this(function, numArguments, null);
  }

  public ArityException(Function function, int numArguments, Throwable cause) {
    super(createMessage(function, numArguments, true), cause);
  }

  public static String createMessage(Function function, int numArguments, boolean initialUppercase) {
    int minArity = function.argumentConstraints().minArity();
    int maxArity = function.argumentConstraints().maxArity();
    StringBuilder buffer = new StringBuilder();
    if (initialUppercase) {
      buffer.append("Invalid");
    } else {
      buffer.append("invalid");
    }
    buffer.append(" arity calling \"").append(function.name()).append("\"");
    if (maxArity == minArity) {
      buffer.append(String.format(" (expected %d but was %d)", minArity, numArguments));
    } else if (numArguments < minArity) {
      buffer.append(String.format(" (expected at least %d but was %d)", minArity, numArguments));
    } else {
      buffer.append(String.format(" (expected at most %d but was %d)", maxArity, numArguments));
    }
    return buffer.toString();
  }
}
