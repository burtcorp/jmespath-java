package io.burt.jmespath;

@SuppressWarnings("serial")
public class JmesPathException extends RuntimeException {
  public JmesPathException(String message) {
    super(message);
  }

  public JmesPathException(String message, Throwable cause) {
    super(message, cause);
  }
}
