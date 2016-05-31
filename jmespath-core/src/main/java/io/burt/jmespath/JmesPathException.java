package io.burt.jmespath;

public class JmesPathException extends RuntimeException {
  public JmesPathException(String message) {
    super(message);
  }

  public JmesPathException(String message, Throwable cause) {
    super(message, cause);
  }
}
