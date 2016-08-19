package io.burt.jmespath.function;

@SuppressWarnings("serial")
public class ArgumentTypeException extends FunctionCallException {
  public ArgumentTypeException(String functionName, String expectedType, String actualType) {
    this(functionName, expectedType, actualType, null);
  }

  public ArgumentTypeException(String functionName, String expectedType, String actualType, Throwable cause) {
    super(String.format("Wrong type of argument calling %s: expected %s but was %s", functionName, expectedType, actualType), cause);
  }
}
