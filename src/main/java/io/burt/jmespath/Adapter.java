package io.burt.jmespath;

import java.util.List;

public interface Adapter<T> {
  List<T> explode(T value);

  T combine(List<T> elements);

  T getProperty(T value, String name);

  T getIndex(T value, int index);

  T createArray(List<T> elements);

  T createString(String str);
}
