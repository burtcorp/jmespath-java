package io.burt.jmespath;

import io.burt.jmespath.node.JmesPathNode;

public class JmesPathExpression<T> {
  private final JmesPathRuntime<T> runtime;
  private final JmesPathNode expression;

  public JmesPathExpression(JmesPathRuntime<T> runtime, JmesPathNode expression) {
    this.runtime = runtime;
    this.expression = expression;
  }

  public T search(T input) {
    return expression.evaluate(runtime, input);
  }

  protected JmesPathRuntime runtime() {
    return runtime;
  }

  protected JmesPathNode expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("Expression(%s)", expression);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof JmesPathExpression)) {
      return false;
    }
    JmesPathExpression other = (JmesPathExpression) o;
    return this.runtime().equals(other.runtime()) && this.expression().equals(other.expression());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + runtime.hashCode();
    h = h * 31 + expression.hashCode();
    return h;
  }
}
