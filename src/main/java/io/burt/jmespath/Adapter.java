package io.burt.jmespath;

import java.util.List;

public interface Adapter<T> {
  List<T> toList(T array);

  T combine(List<T> elements);

  boolean isArray(T value);

  T getProperty(T value, String name);

  T getIndex(T value, int index);

  T createArray(List<T> elements);

  T createString(String str);
}
