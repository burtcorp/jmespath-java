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
  public Node<T> createSequence(List<Node<T>> nodes) {
    return new SequenceNode<>(runtime, nodes);
  }

  @Override
  public Node<T> createProperty(String name) {
    return new PropertyNode<>(runtime, name);
  }

  @Override
  public Node<T> createIndex(int index) {
    return new IndexNode<>(runtime, index);
  }

  @Override
  public Node<T> createSlice(Integer start, Integer stop, Integer step) {
    return new SliceNode<>(runtime, start, stop, step);
  }

  @Override
  public Node<T> createProjection(Expression<T> expression) {
    return new ProjectionNode<>(runtime, expression);
  }

  @Override
  public Node<T> createFlattenArray() {
    return new FlattenArrayNode<>(runtime);
  }

  @Override
  public Node<T> createFlattenObject() {
    return new FlattenObjectNode<>(runtime);
  }

  @Override
  public Node<T> createSelection(Expression<T> test) {
    return new SelectionNode<>(runtime, test);
  }

  @Override
  public Node<T> createComparison(Operator operator, Expression<T> left, Expression<T> right) {
    return ComparisonNode.create(runtime, operator, left, right);
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
  public Node<T> createFunctionCall(String functionName, List<? extends Expression<T>> args) {
    return new FunctionCallNode<>(runtime, runtime.functionRegistry().getFunction(functionName), args);
  }

  @Override
  public Node<T> createFunctionCall(Function function, List<? extends Expression<T>> args) {
    return new FunctionCallNode<>(runtime, function, args);
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
  public Node<T> createNegate(Expression<T> negated) {
    return new NegateNode<>(runtime, negated);
  }

  @Override
  public Node<T> createCreateObject(List<CreateObjectNode.Entry<T>> entries) {
    return new CreateObjectNode<>(runtime, entries);
  }

  @Override
  public Node<T> createCreateArray(List<? extends Expression<T>> entries) {
    return new CreateArrayNode<>(runtime, entries);
  }

  @Override
  public Node<T> createJsonLiteral(String json) {
    return new JsonLiteralNode<>(runtime, json);
  }
}
