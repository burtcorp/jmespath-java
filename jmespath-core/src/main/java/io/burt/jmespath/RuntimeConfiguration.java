package io.burt.jmespath;

import io.burt.jmespath.function.FunctionRegistry;

public class RuntimeConfiguration {
  private final FunctionRegistry functionRegistry;

  private RuntimeConfiguration(Builder builder) {
    this.functionRegistry = builder.functionRegistry;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static RuntimeConfiguration defaultConfiguration() {
    return builder().build();
  }

  public static class Builder {
    protected FunctionRegistry functionRegistry;

    public Builder() {
      this.functionRegistry = FunctionRegistry.defaultRegistry();
    }

    public RuntimeConfiguration build() {
      return new RuntimeConfiguration(this);
    }

    public Builder withFunctionRegistry(FunctionRegistry functionRegistry) {
      this.functionRegistry = functionRegistry;
      return this;
    }
  }

  public FunctionRegistry functionRegistry() {
    return functionRegistry;
  }
}
