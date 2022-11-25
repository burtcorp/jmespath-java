package io.burt.jmespath.node;

public enum ArithmeticOperator {
  ADDITION,
  SUBSTRACTION,
  MULTIPLICATION,
  DIVISION,
  MODULUS;
  
  public static ArithmeticOperator fromString(String str) {
    if ("+".equals(str)) {
      return ADDITION;
    } else if ("-".equals(str)) {
      return SUBSTRACTION;
    } else if ("*".equals(str)) {
      return MULTIPLICATION;
    } else if ("/".equals(str)) {
      return DIVISION;
    } else if ("%".equals(str)) {
      return MODULUS;
    } else {
      throw new IllegalArgumentException(String.format("No such operator %s", str));
    }
  }
}
