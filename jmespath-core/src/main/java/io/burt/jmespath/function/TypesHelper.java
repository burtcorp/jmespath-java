package io.burt.jmespath.function;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

class TypesHelper {
  private TypesHelper() { }

  static <T> boolean isNumberArray(Adapter<T> adapter, T array) {
    if (adapter.typeOf(array) == JmesPathType.ARRAY) {
      for (T element : adapter.toList(array)) {
        if (adapter.typeOf(element) != JmesPathType.NUMBER) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

  static <T> boolean isStringArray(Adapter<T> adapter, T array) {
    if (adapter.typeOf(array) == JmesPathType.ARRAY) {
      for (T element : adapter.toList(array)) {
        if (adapter.typeOf(element) != JmesPathType.STRING) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
