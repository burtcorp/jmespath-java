package io.burt.jmespath.function;

import io.burt.jmespath.JmesPathException;

@SuppressWarnings("serial")
public class FunctionConfigurationException extends JmesPathException {
  public FunctionConfigurationException(String message) {
    super(message);
  }
}
