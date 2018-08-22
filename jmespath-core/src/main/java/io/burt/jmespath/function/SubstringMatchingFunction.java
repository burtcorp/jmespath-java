package io.burt.jmespath.function;

public abstract class SubstringMatchingFunction extends BaseFunction {
  public SubstringMatchingFunction(ArgumentConstraint... argumentConstraints) {
    super(argumentConstraints);
  }

  protected static boolean isEmpty(String str) {
    return str == null || str.length() == 0;
  }
}
