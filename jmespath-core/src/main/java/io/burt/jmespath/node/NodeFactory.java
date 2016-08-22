package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;

/**
 * A node factory is used by the expression compiler to create AST nodes.
 */
public interface NodeFactory<T> {
  public Node<T> createCurrent();

  public Node<T> createProperty(String name);

  public Node<T> createIndex(int index);

  public Node<T> createSlice(Integer start, Integer stop, Integer step);

  public Node<T> createProjection(Expression<T> expression);

  public Node<T> createFlattenArray();

  public Node<T> createFlattenObject();

  public Node<T> createSelection(Expression<T> test);

  public Node<T> createComparison(Operator operator, Expression<T> left, Expression<T> right);

  public Node<T> createOr(Expression<T> left, Expression<T> right);

  public Node<T> createAnd(Expression<T> left, Expression<T> right);

  public Node<T> createFunctionCall(String functionName, List<? extends Expression<T>> args);

  public Node<T> createFunctionCall(Function function, List<? extends Expression<T>> args);

  public Node<T> createExpressionReference(Expression<T> expression);

  public Node<T> createString(String str);

  public Node<T> createNegate(Expression<T> negated);

  public Node<T> createCreateObject(List<CreateObjectNode.Entry<T>> entries);

  public Node<T> createCreateArray(List<? extends Expression<T>> entries);

  public Node<T> createJsonLiteral(String json);

  public Node<T> createSequence(List<Node<T>> nodes);
}
