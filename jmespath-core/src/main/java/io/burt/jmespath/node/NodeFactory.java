package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;

/**
 * A node factory is used by the expression compiler to create AST nodes.
 */
public interface NodeFactory<T> {
  Node<T> createCurrent();

  Node<T> createProperty(String name);

  Node<T> createIndex(int index);

  Node<T> createSlice(Integer start, Integer stop, Integer step);

  Node<T> createProjection(Expression<T> expression);

  Node<T> createFlattenArray();

  Node<T> createFlattenObject();

  Node<T> createSelection(Expression<T> test);

  Node<T> createComparison(Operator operator, Expression<T> left, Expression<T> right);

  Node<T> createOr(Expression<T> left, Expression<T> right);

  Node<T> createAnd(Expression<T> left, Expression<T> right);

  Node<T> createArithmetic(ArithmeticOperator operator, Expression<T> left, Expression<T> right);

  Node<T> createFunctionCall(String functionName, List<? extends Expression<T>> args);

  Node<T> createFunctionCall(Function function, List<? extends Expression<T>> args);

  Node<T> createExpressionReference(Expression<T> expression);

  Node<T> createString(String str);

  Node<T> createNumber(Number str);

  Node<T> createNegate(Expression<T> negated);

  Node<T> createCreateObject(List<CreateObjectNode.Entry<T>> entries);

  Node<T> createCreateArray(List<? extends Expression<T>> entries);

  Node<T> createJsonLiteral(String json);

  Node<T> createSequence(List<Node<T>> nodes);
}
