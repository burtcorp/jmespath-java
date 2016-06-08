package io.burt.jmespath.function;

public class ArgumentTypeException extends FunctionCallException {
  public ArgumentTypeException(String functionName, String expectedType, String receivedType) {
    super(String.format("Wrong type of argument calling %s: expected %s but was %s", functionName, expectedType, receivedType));
  }
}
