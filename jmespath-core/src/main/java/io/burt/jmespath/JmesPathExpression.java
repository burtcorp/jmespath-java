package io.burt.jmespath;

import io.burt.jmespath.parser.JmesPathExpressionParser;
import io.burt.jmespath.node.JmesPathNode;

public class JmesPathExpression {
  private final JmesPathNode expression;

  public JmesPathExpression(JmesPathNode expression) {
    this.expression = expression;
  }

  public static JmesPathExpression fromString(String expression) {
    return fromString(null, expression);
  }

  public static <T> JmesPathExpression fromString(JmesPathRuntime<T> runtime, String expression) {
    return JmesPathExpressionParser.fromString(expression, runtime);
  }

  public <T> T evaluate(JmesPathRuntime<T> runtime, T input) {
    return expression.evaluate(runtime, input);
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
    return this.expression().equals(other.expression());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + expression.hashCode();
    return h;
  }
}
