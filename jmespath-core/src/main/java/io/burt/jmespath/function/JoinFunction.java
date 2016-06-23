package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import io.burt.jmespath.Adapter;

public class JoinFunction extends JmesPathFunction {
  public JoinFunction() {
    super(2, 2);
  }

  @Override
  protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    ExpressionOrValue<T> firstArgument = arguments.get(0);
    ExpressionOrValue<T> secondArgument = arguments.get(1);
    if (firstArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "string", "expression");
    } else if (secondArgument.isExpression()) {
      throw new ArgumentTypeException(name(), "array of strings", "expression");
    } else {
      T glue = firstArgument.value();
      T components = secondArgument.value();
      if (isStringArray(adapter, components)) {
        if (adapter.isString(glue)) {
          Iterator<T> values = adapter.toList(components).iterator();
          if (values.hasNext()) {
            StringBuilder buffer = new StringBuilder();
            String glueString = adapter.toString(glue);
            buffer.append(adapter.toString(values.next()));
            while (values.hasNext()) {
              buffer.append(glueString);
              buffer.append(adapter.toString(values.next()));
            }
            return adapter.createString(buffer.toString());
          } else {
            return adapter.createString("");
          }
        } else {
          throw new ArgumentTypeException(name(), "string", adapter.typeOf(glue).toString());
        }
      } else {
        List<T> values = adapter.toList(components);
        List<String> types = new ArrayList<>(values.size());
        for (T value : values) {
          types.add(adapter.typeOf(value).toString());
        }
        throw new ArgumentTypeException(name(), "array of strings", types.toString());
      }
    }
  }

  private <T> boolean isStringArray(Adapter<T> adapter, T array) {
    if (adapter.isArray(array)) {
      for (T value : adapter.toList(array)) {
        if (!adapter.isString(value)) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }
}
