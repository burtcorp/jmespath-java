package io.burt.jmespath.function;

public class MaxFunction extends CompareFunction {
  @Override
  protected boolean sortsBefore(int compareResult) {
    return compareResult > 0;
  }
}
