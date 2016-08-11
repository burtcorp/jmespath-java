package io.burt.jmespath.function;

import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public class MapFunction extends BaseFunction {
  public MapFunction() {
    super(
      ArgumentConstraints.expression(),
      ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.OBJECT))
    );
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    Expression<T> expression = arguments.get(0).expression();
    T array = arguments.get(1).value();
    List<T> elements = runtime.toList(array);
    List<T> result = new ArrayList<>(elements.size());
    for (T element : elements) {
      result.add(expression.search(element));
    }
    return runtime.createArray(result);
  }
}
