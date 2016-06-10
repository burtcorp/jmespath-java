package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;

class TypesHelper {
  private TypesHelper() { }

  static <T> boolean isNumberArray(Adapter<T> adapter, T array) {
    if (adapter.isArray(array)) {
      for (T element : adapter.toList(array)) {
        if (!adapter.isNumber(element)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  static <T> boolean isStringArray(Adapter<T> adapter, T array) {
    if (adapter.isArray(array)) {
      for (T element : adapter.toList(array)) {
        if (!adapter.isString(element)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
