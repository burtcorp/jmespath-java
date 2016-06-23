package io.burt.jmespath;

public enum ValueType {
  NUMBER,
  STRING,
  BOOLEAN,
  ARRAY,
  OBJECT,
  NULL;

  public String toString() {
    return name().toLowerCase();
  }
}
