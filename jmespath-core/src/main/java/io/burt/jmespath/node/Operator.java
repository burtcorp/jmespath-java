package io.burt.jmespath.node;

public enum Operator {
  EQUALS,
  NOT_EQUALS,
  GREATER_THAN,
  GREATER_THAN_OR_EQUALS,
  LESS_THAN,
  LESS_THAN_OR_EQUALS;

  public static Operator fromString(String str) {
    if ("==".equals(str)) {
      return EQUALS;
    } else if ("!=".equals(str)) {
      return NOT_EQUALS;
    } else if (">".equals(str)) {
      return GREATER_THAN;
    } else if (">=".equals(str)) {
      return GREATER_THAN_OR_EQUALS;
    } else if ("<".equals(str)) {
      return LESS_THAN;
    } else if ("<=".equals(str)) {
      return LESS_THAN_OR_EQUALS;
    } else {
      return null;
    }
  }
}
