package io.burt.jmespath.function;

public class MinByFunction extends CompareByFunction {
  @Override
  protected boolean sortsBefore(int compareResult) {
    return compareResult < 0;
  }
}
