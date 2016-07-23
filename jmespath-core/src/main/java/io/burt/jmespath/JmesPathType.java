package io.burt.jmespath;

public enum JmesPathType {
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
