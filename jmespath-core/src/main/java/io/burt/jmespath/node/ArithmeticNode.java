package io.burt.jmespath.node;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;

public abstract class ArithmeticNode<T> extends OperatorNode<T> {
  public static class AdditionNode<T> extends ArithmeticNode<T> {
    public AdditionNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }
    
    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      
      Number left = runtime.toNumber(leftResult);
      Number right = runtime.toNumber(rightResult);
      if (left instanceof Long && right instanceof Long) {
        long result = left.longValue() + right.longValue();
        return runtime.createNumber(result);
      } else {
        double result = left.doubleValue() + right.doubleValue();
        return runtime.createNumber(result);
      }
      
    }
    
    @Override
    protected String operatorToString() {
      return "+";
    }
  }
  
  public static class SubtractionNode<T> extends ArithmeticNode<T> {
    public SubtractionNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }
    
    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      Number left = runtime.toNumber(leftResult);
      Number right = runtime.toNumber(rightResult);
      if (left instanceof Long && right instanceof Long) {
        long result = left.longValue() - right.longValue();
        return runtime.createNumber(result);
      } else {
        double result = left.doubleValue() - right.doubleValue();
        return runtime.createNumber(result);
      }
    }
    
    @Override
    protected String operatorToString() {
      return "-";
    }
  }
  
  public static class MultiplicationNode<T> extends ArithmeticNode<T> {
    public MultiplicationNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }
    
    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      Number left = runtime.toNumber(leftResult);
      Number right = runtime.toNumber(rightResult);
      if (left instanceof Long && right instanceof Long) {
        long result = left.longValue() * right.longValue();
        return runtime.createNumber(result);
      } else {
        double result = left.doubleValue() * right.doubleValue();
        return runtime.createNumber(result);
      }
    }
    
    @Override
    protected String operatorToString() {
      return "*";
    }
  }
  
  public static class DivisionNode<T> extends ArithmeticNode<T> {
    public DivisionNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }
    
    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      Number left = runtime.toNumber(leftResult);
      Number right = runtime.toNumber(rightResult);
      if (left instanceof Long && right instanceof Long) {
        long result = left.longValue() / right.longValue();
        return runtime.createNumber(result);
      } else {
        double result = left.doubleValue() / right.doubleValue();
        return runtime.createNumber(result);
      }
    }
    
    @Override
    protected String operatorToString() {
      return "/";
    }
  }
  
  public static class ModulusNode<T> extends ArithmeticNode<T> {
    public ModulusNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
      super(runtime, left, right);
    }
    
    @Override
    protected T compareObjects(T leftResult, T rightResult) {
      Number left = runtime.toNumber(leftResult);
      Number right = runtime.toNumber(rightResult);
      if (left instanceof Long && right instanceof Long) {
        long result = left.longValue() % right.longValue();
        return runtime.createNumber(result);
      } else {
        double result = left.doubleValue() % right.doubleValue();
        return runtime.createNumber(result);
      }
    }
    
    @Override
    protected String operatorToString() {
      return "%";
    }
  }
  
  protected ArithmeticNode(Adapter<T> runtime, Expression<T> left, Expression<T> right) {
    super(runtime, left, right);
  }
  
  public static <U> Node<U> create(Adapter<U> runtime, ArithmeticOperator operator, Expression<U> left, Expression<U> right) {
    switch (operator) {
    case ADDITION:
      return new AdditionNode<>(runtime, left, right);
    case SUBSTRACTION:
      return new SubtractionNode<>(runtime, left, right);
    case MULTIPLICATION:
      return new MultiplicationNode<>(runtime, left, right);
    case DIVISION:
      return new DivisionNode<>(runtime, left, right);
    case MODULUS:
      return new DivisionNode<>(runtime, left, right);
    default:
      throw new IllegalStateException(String.format("Unknown operator encountered: %s", operator));
    }
  }
  
  @Override
  public T search(T input) {
    T leftResult = operand(0).search(input);
    T rightResult = operand(1).search(input);
    return compareObjects(leftResult, rightResult);
  }
  
  protected Number comparisonResult(T leftResult, T rightResult) {
    return null;
  }
  
  protected T compareObjects(T leftResult, T rightResult) {
    return runtime.createNull();
  }
  
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
