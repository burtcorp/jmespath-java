package io.burt.jmespath.function;

@SuppressWarnings("serial")
public class ArityException extends FunctionCallException {
  public ArityException(String functionName, int minArity, int maxArity, int numArguments) {
    super(createMessage(functionName, minArity, maxArity, numArguments));
  }

  private static String createMessage(String functionName, int minArity, int maxArity, int numArguments) {
    if (maxArity == minArity) {
      return String.format("Wrong number of arguments calling %s: expected %d but was %d", functionName, minArity, numArguments);
    } else if (numArguments < minArity) {
      return String.format("Wrong number of arguments calling %s: expected at least %d but was %d", functionName, minArity, numArguments);
    } else {
      return String.format("Wrong number of arguments calling %s: expected at most %d but was %d", functionName, maxArity, numArguments);
    }
  }
}
