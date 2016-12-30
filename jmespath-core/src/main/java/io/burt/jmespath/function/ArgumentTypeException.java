package io.burt.jmespath.function;

@SuppressWarnings("serial")
public class ArgumentTypeException extends FunctionCallException {
  public ArgumentTypeException(Function function, String expectedType, String actualType) {
    this(function, expectedType, actualType, null);
  }

  public ArgumentTypeException(Function function, String expectedType, String actualType, Throwable cause) {
    super(String.format("Invalid argument type calling \"%s\": expected %s but was %s", function.name(), expectedType, actualType), cause);
  }
}
