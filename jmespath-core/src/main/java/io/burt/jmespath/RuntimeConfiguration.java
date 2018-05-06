package io.burt.jmespath;

import io.burt.jmespath.function.FunctionRegistry;

public class RuntimeConfiguration {
  private final FunctionRegistry functionRegistry;
  private final boolean silentTypeErrors;

  private RuntimeConfiguration(Builder builder) {
    this.functionRegistry = builder.functionRegistry;
    this.silentTypeErrors = builder.silentTypeErrors;
  }

  public FunctionRegistry functionRegistry() {
    return functionRegistry;
  }

  public boolean silentTypeErrors() {
    return silentTypeErrors;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static RuntimeConfiguration defaultConfiguration() {
    return builder().build();
  }

  public static class Builder {
    protected FunctionRegistry functionRegistry;
    protected boolean silentTypeErrors;

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

    public Builder withSilentTypeErrors(boolean silentTypeErrors) {
      this.silentTypeErrors = silentTypeErrors;
      return this;
    }
  }
}
