package io.burt.jmespath;

import io.burt.jmespath.node.JmesPathNode;

public class StandardExpression<T> implements JmesPathExpression<T> {
  private final Adapter<T> runtime;
  private final JmesPathNode<T> expression;

  public StandardExpression(Adapter<T> runtime, JmesPathNode<T> expression) {
    this.runtime = runtime;
    this.expression = expression;
  }

  public T search(T input) {
    return expression.evaluate(input);
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
    if (!(o instanceof StandardExpression)) {
      return false;
    }
    StandardExpression other = (StandardExpression) o;
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
