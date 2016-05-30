package io.burt.jmespath;

public class FunctionCallException extends JmesPathException {
  public FunctionCallException(String message) {
    super(message);
  }

  public FunctionCallException(String message, Throwable cause) {
    super(message, cause);
  }
}
