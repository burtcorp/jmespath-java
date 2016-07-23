package io.burt.jmespath;

import io.burt.jmespath.parser.JmesPathQueryParser;
import io.burt.jmespath.node.JmesPathNode;

public class Query {
  private final JmesPathNode expression;

  public Query(JmesPathNode expression) {
    this.expression = expression;
  }

  public static Query fromString(String query) {
    return fromString(null, query);
  }

  public static <T> Query fromString(Adapter<T> adapter, String query) {
    return JmesPathQueryParser.fromString(query, adapter);
  }

  public <T> T evaluate(Adapter<T> adapter, T input) {
    return expression.evaluate(adapter, input);
  }

  protected JmesPathNode expression() {
    return expression;
  }

  @Override
  public String toString() {
    return String.format("Query(%s)", expression);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Query)) {
      return false;
    }
    Query other = (Query) o;
    return this.expression().equals(other.expression());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + expression.hashCode();
    return h;
  }
}
