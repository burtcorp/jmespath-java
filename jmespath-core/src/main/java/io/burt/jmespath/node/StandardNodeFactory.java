package io.burt.jmespath.node;

import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.function.Function;

/**
 * This node factory creates instances of the standard node classes.
 */
public class StandardNodeFactory<T> implements NodeFactory<T> {
  private final Adapter<T> runtime;

  public StandardNodeFactory(Adapter<T> runtime) {
    this.runtime = runtime;
  }

  @Override
  public Node<T> createCurrent() {
    return new CurrentNode<>(runtime);
  }

  @Override
  public Node<T> createCurrent(Node<T> source) {
    return new CurrentNode<>(runtime, source);
  }

  @Override
  public Node<T> createProperty(String name, Node<T> source) {
    return new PropertyNode<>(runtime, name, source);
  }

  @Override
  public Node<T> createIndex(int index, Node<T> source) {
    return new IndexNode<>(runtime, index, source);
  }

  @Override
  public Node<T> createSlice(Integer start, Integer stop, Integer step, Node<T> source) {
    return new SliceNode<>(runtime, start, stop, step, source);
  }

  @Override
  public Node<T> createProjection(Expression<T> expression, Node<T> source) {
    return new ProjectionNode<>(runtime, expression, source);
  }

  @Override
  public Node<T> createFlattenArray(Node<T> source) {
    return new FlattenArrayNode<>(runtime, source);
  }

  @Override
  public Node<T> createFlattenObject(Node<T> source) {
    return new FlattenObjectNode<>(runtime, source);
  }

  @Override
  public Node<T> createSelection(Expression<T> test, Node<T> source) {
    return new SelectionNode<>(runtime, test, source);
  }

  @Override
  public Node<T> createComparison(String operator, Expression<T> left, Expression<T> right) {
    return new ComparisonNode<>(runtime, operator, left, right);
  }

  @Override
  public Node<T> createOr(Expression<T> left, Expression<T> right) {
    return new OrNode<>(runtime, left, right);
  }

  @Override
  public Node<T> createAnd(Expression<T> left, Expression<T> right) {
    return new AndNode<>(runtime, left, right);
  }

  @Override
  public Node<T> createFunctionCall(String functionName, List<? extends Expression<T>> args, Node<T> source) {
    return new FunctionCallNode<>(runtime, runtime.functionRegistry().getFunction(functionName), args, source);
  }

  @Override
  public Node<T> createFunctionCall(Function function, List<? extends Expression<T>> args, Node<T> source) {
    return new FunctionCallNode<>(runtime, function, args, source);
  }

  @Override
  public Node<T> createExpressionReference(Expression<T> expression) {
    return new ExpressionReferenceNode<>(runtime, expression);
  }

  @Override
  public Node<T> createString(String str) {
    return new StringNode<>(runtime, str);
  }

  @Override
  public Node<T> createNegate(Node<T> source) {
    return new NegateNode<>(runtime, source);
  }

  @Override
  public Node<T> createCreateObject(List<CreateObjectNode.Entry<T>> entries, Node<T> source) {
    return new CreateObjectNode<>(runtime, entries, source);
  }

  @Override
  public Node<T> createCreateArray(List<? extends Expression<T>> entries, Node<T> source) {
    return new CreateArrayNode<>(runtime, entries, source);
  }

  @Override
  public Node<T> createJsonLiteral(String json) {
    return new JsonLiteralNode<>(runtime, json, runtime.parseString(json));
  }
}
