package io.burt.jmespath;

import java.util.List;
import java.util.Comparator;

public interface Adapter<T> extends Comparator<T> {
  List<T> toList(T array);

  boolean isArray(T value);

  boolean isObject(T value);

  boolean isNumber(T value);

  boolean isTruthy(T value);

  T getProperty(T value, String name);

  T createNull();

  T createArray(List<T> elements);

  T createArray(List<T> elements, boolean compact);

  T createString(String str);

  T createBoolean(boolean b);
}
