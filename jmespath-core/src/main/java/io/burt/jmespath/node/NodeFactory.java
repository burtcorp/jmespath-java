package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;

/**
 * A node factory is used by the expression compiler to create AST nodes.
 */
public interface NodeFactory<T> {
  public Node<T> createCurrent();

  public Node<T> createCurrent(Node<T> source);

  public Node<T> createProperty(String name, Node<T> source);

  public Node<T> createIndex(int index, Node<T> source);

  public Node<T> createSlice(Integer start, Integer stop, Integer step, Node<T> source);

  public Node<T> createProjection(Expression<T> expression, Node<T> source);

  public Node<T> createFlattenArray(Node<T> source);

  public Node<T> createFlattenObject(Node<T> source);

  public Node<T> createSelection(Expression<T> test, Node<T> source);

  public Node<T> createComparison(String operator, Expression<T> left, Expression<T> right);

  public Node<T> createOr(Expression<T> left, Expression<T> right);

  public Node<T> createAnd(Expression<T> left, Expression<T> right);

  public Node<T> createFunctionCall(String functionName, List<? extends Expression<T>> args, Node<T> source);

  public Node<T> createFunctionCall(Function function, List<? extends Expression<T>> args, Node<T> source);

  public Node<T> createExpressionReference(Expression<T> expression);

  public Node<T> createString(String str);

  public Node<T> createNegate(Node<T> source);

  public Node<T> createCreateObject(List<CreateObjectNode.Entry<T>> entries, Node<T> source);

  public Node<T> createCreateArray(List<? extends Expression<T>> entries, Node<T> source);

  public Node<T> createJsonLiteral(String json);
}
