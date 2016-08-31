package io.burt.jmespath.function;

public class ArgumentError {
  public static class ArgumentTypeError extends ArgumentError {
    private final String expectedType;
    private final String actualType;

    public ArgumentTypeError(String expectedType, String actualType) {
      this.expectedType = expectedType;
      this.actualType = actualType;
    }

    public String expectedType() { return expectedType; }

    public String actualType() { return actualType; }
  }

  public static class ArityError extends ArgumentError { }

  public static ArgumentError createArityError() {
    return new ArityError();
  }

  public static ArgumentTypeError createArgumentTypeError(String expectedType, String actualType) {
    return new ArgumentTypeError(expectedType, actualType);
  }
}
