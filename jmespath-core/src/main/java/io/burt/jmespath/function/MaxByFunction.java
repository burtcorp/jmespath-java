package io.burt.jmespath.function;

public class MaxByFunction extends CompareByFunction {
  @Override
  protected boolean sortsBefore(int compareResult) {
    return compareResult > 0;
  }
}
