package io.burt.jmespath.function;

import java.util.List;
import java.util.Iterator;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class JoinFunction extends BaseFunction {
  public JoinFunction() {
    super(
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.STRING))
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<ExpressionOrValue<T>> arguments) {
    T glue = arguments.get(0).value();
    T components = arguments.get(1).value();
    Iterator<T> values = runtime.toList(components).iterator();
    if (values.hasNext()) {
      StringBuilder buffer = new StringBuilder();
      String glueString = runtime.toString(glue);
      buffer.append(runtime.toString(values.next()));
      while (values.hasNext()) {
        buffer.append(glueString);
        buffer.append(runtime.toString(values.next()));
      }
      return runtime.createString(buffer.toString());
    } else {
      return runtime.createString("");
    }
  }
}
