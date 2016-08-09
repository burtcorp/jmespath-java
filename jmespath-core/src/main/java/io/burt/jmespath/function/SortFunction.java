package io.burt.jmespath.function;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;

public class SortFunction extends JmesPathFunction {
  public SortFunction() {
    super(
      ArgumentConstraints.arrayOf(
        ArgumentConstraints.typeOf(JmesPathType.NUMBER, JmesPathType.STRING)
      )
    );
  }

  @Override
  protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
    List<T> elements = new ArrayList<T>(runtime.toList(arguments.get(0).value()));
    Collections.sort(elements, runtime);
    return runtime.createArray(elements);
  }
}
