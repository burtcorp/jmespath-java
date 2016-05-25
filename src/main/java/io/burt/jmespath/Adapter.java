package io.burt.jmespath;

import java.util.List;

public interface Adapter<T> {
  List<T> toList(T array);

  boolean isArray(T value);

  T getProperty(T value, String name);

  T createNull();

  T createArray(List<T> elements);

  T createArray(List<T> elements, boolean compact);

  T createString(String str);
}
