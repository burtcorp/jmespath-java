package io.burt.jmespath.function;

import io.burt.jmespath.JmesPathException;

/** Base class of exceptions thrown when functions are called */
@SuppressWarnings("serial")
public class FunctionCallException extends JmesPathException {
  public FunctionCallException(String message) {
    super(message);
  }

  public FunctionCallException(String message, Throwable cause) {
    super(message, cause);
  }
}
