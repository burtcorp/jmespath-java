package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.JmesPathType;

public abstract class ComparisonNode<T> extends OperatorNode<T> {
  public static class EqualsNode<T> extends ComparisonNode<T> {
    public EqualsNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) == 0);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) == 0);
    }

    @Override
    protected String operatorToString() {
      return "==";
    }
  }

  public static class NotEqualsNode<T> extends ComparisonNode<T> {
    public NotEqualsNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) != 0);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) != 0);
    }

    @Override
    protected String operatorToString() {
      return "!=";
    }
  }

  public static class GreaterThanNode<T> extends ComparisonNode<T> {
    public GreaterThanNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) > 0);
    }

    @Override
    protected String operatorToString() {
      return ">";
    }
  }

  public static class GreaterThanOrEqualsNode<T> extends ComparisonNode<T> {
    public GreaterThanOrEqualsNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) >= 0);
    }

    @Override
    protected String operatorToString() {
      return ">=";
    }
  }

  public static class LessThanNode<T> extends ComparisonNode<T> {
    public LessThanNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) < 0);
    }

    @Override
    protected String operatorToString() {
      return "<";
    }
  }

  public static class LessThanOrEqualsNode<T> extends ComparisonNode<T> {
    public LessThanOrEqualsNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }

    @Override
    protected T compareNumbers(T leftResult, T rightResult) {
      return runtime.createBoolean(comparisonResult(leftResult, rightResult) <= 0);
    }

    @Override
    protected String operatorToString() {
      return "<=";
    }
  }

  protected ComparisonNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
    super(runtime, left, right);
  }

  public static <U> Node<U> create(Adapter<U> runtime, Operator operator, Expression<U> left, Expression<U> right) {
    switch (operator) {
      case EQUALS:
        return new EqualsNode<>(runtime, left, right);
      case NOT_EQUALS:
        return new NotEqualsNode<>(runtime, left, right);
      case GREATER_THAN:
        return new GreaterThanNode<>(runtime, left, right);
      case GREATER_THAN_OR_EQUALS:
        return new GreaterThanOrEqualsNode<>(runtime, left, right);
      case LESS_THAN:
        return new LessThanNode<>(runtime, left, right);
      case LESS_THAN_OR_EQUALS:
        return new LessThanOrEqualsNode<>(runtime, left, right);
      default:
        throw new IllegalStateException(String.format("Unknown operator encountered: %s", operator));
    }
  }

  @Override
  public T search(T input) {
    T leftResult = operand(0).search(input);
    T rightResult = operand(1).search(input);
    JmesPathType leftType = runtime.typeOf(leftResult);
    JmesPathType rightType = runtime.typeOf(rightResult);
    if (leftType == JmesPathType.NUMBER && rightType == JmesPathType.NUMBER) {
      return compareNumbers(leftResult, rightResult);
    } else {
      return compareObjects(leftResult, rightResult);
    }
  }

  protected int comparisonResult(T leftResult, T rightResult) {
    return runtime.compare(leftResult, rightResult);
  }

  protected T compareObjects(T leftResult, T rightResult) {
    return runtime.createNull();
  }

  protected abstract T compareNumbers(T leftResult, T rightResult);

  @Override
  protected String internalToString() {
    return String.format("%s, %s, %s", operatorToString(), operand(0), operand(1));
  }

  protected abstract String operatorToString();

  @Override
  protected int internalHashCode() {
    return operatorToString().hashCode();
  }
}
