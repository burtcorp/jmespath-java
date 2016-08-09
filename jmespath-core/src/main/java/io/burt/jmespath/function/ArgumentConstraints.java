package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

/**
 * A DSL for describing argument constraints of functions.
 * <p>
 * For example, this is how <code>join</code> describes its arguments:
 * <p>
 * <pre>
 * public JoinFunction() {
 *   super(
 *     ArgumentConstraints.typeOf(JmesPathType.STRING),
 *     ArgumentConstraints.arrayOf(ArgumentConstraints.typeOf(JmesPathType.STRING))
 *   );
 * }
 * </pre>
 * I.e. accept exactly two argument, where the first must be a string and the
 * second must be an array of strings.
 * <p>
 * The static methods of this class can be used to compose constraints for most
 * situations, but you can also create your own constraints (that can be combined
 * with other constraints) by implementing the {@link ArgumentConstraint}
 * interface.
 */
public final class ArgumentConstraints {
  /**
   * Describes a heterogenous list of arguments. Each argument is checked against
   * the corresponding constraint. An {@link ArityException} will be thrown when
   * the number of arguments does not exactly match the number of constraints.
   * <p>
   * May only be used as a top level constraint – and is already built in to
   * {@link JmesPathFunction}, so direct usage of this method should not be needed.
   */
  public static ArgumentConstraint listOf(ArgumentConstraint... constraints) {
    return new HeterogenousListOf(constraints);
  }

  /**
   * Descripes a homogenous list of arguments, of fixed or variable length.
   * An {@link ArityException} will be thrown when there are fewer arguments
   * than the specified minimum arity, or when there are more arguments than
   * the specified maximum arity.
   * <p>
   * May only be used as a top level constraint.
   */
  public static ArgumentConstraint listOf(int min, int max, ArgumentConstraint constraint) {
    return new HomogenousListOf(min, max, constraint);
  }

  /**
   * Describes a single argument of any value. An {@link ArgumentTypeException}
   * will be thrown when the argument is an expression.
   */
  public static ArgumentConstraint anyValue() {
    return new AnyValue();
  }

  /**
   * Describes a single argument of a specified value type. An {@link ArgumentTypeException}
   * will be thrown when the argument is of the wrong type (as determined by
   * {@link Adapter#typeOf}) or is an expression.
   */
  public static ArgumentConstraint typeOf(JmesPathType type) {
    return new TypeOf(type);
  }

  /**
   * Describes a single argument that is of one of the specified value types.
   * An {@link ArgumentTypeException} will be thrown when the argument is not of
   * one of the specified types (as determined by {@link Adapter#typeOf}) or is
   * an expression.
   */
  public static ArgumentConstraint typeOf(JmesPathType... types) {
    return new TypeOfEither(types);
  }

  /**
   * Describes a single argument that is an array. Each element in the array
   * will be checked against the specified constraint. An {@link ArgumentTypeException}
   * is thrown when the argument does not represent an array value.
   */
  public static ArgumentConstraint arrayOf(ArgumentConstraint constraint) {
    return new ArrayOf(constraint);
  }

  /**
   * Describes a single expression argument. An {@link ArgumentTypeException}
   * will be thrown when the argument is not an expression.
   */
  public static ArgumentConstraint expression() {
    return new Expression();
  }

  static class InternalArgumentTypeException extends FunctionCallException {
    private final String expectedType;
    private final String actualType;

    public InternalArgumentTypeException(String expectedType, String actualType) {
      super("");
      this.expectedType = expectedType;
      this.actualType = actualType;
    }

    public String expectedType() { return expectedType; }

    public String actualType() { return actualType; }
  }

  static class InternalArityException extends FunctionCallException {
    public InternalArityException() {
      super("");
    }
  }

  private static class HomogenousListOf implements ArgumentConstraint {
    private final ArgumentConstraint subConstraint;
    private final int minArity;
    private final int maxArity;

    public HomogenousListOf(int minArity, int maxArity, ArgumentConstraint subConstraint) {
      this.subConstraint = subConstraint;
      this.minArity = minArity;
      this.maxArity = maxArity;
    }

    @Override
    public <T> void check(Adapter<T> adapter, Iterator<ExpressionOrValue<T>> arguments) {
      int i = 0;
      for (; i < minArity; i++) {
        if (!arguments.hasNext()) {
          throw new InternalArityException();
        } else {
          subConstraint.check(adapter, arguments);
        }
      }
      for (; i < maxArity; i++) {
        if (arguments.hasNext()) {
          subConstraint.check(adapter, arguments);
        } else {
          break;
        }
      }
    }

    @Override
    public int minArity() {
      return minArity;
    }

    @Override
    public int maxArity() {
      return maxArity;
    }

    @Override
    public String expectedType() {
      return subConstraint.expectedType();
    }
  }

  private static class HeterogenousListOf implements ArgumentConstraint {
    private final ArgumentConstraint[] subConstraints;
    private final int minArity;
    private final int maxArity;

    public HeterogenousListOf(ArgumentConstraint[] subConstraints) {
      this.subConstraints = subConstraints;
      int min = 0;
      int max = 0;
      for (ArgumentConstraint constraint : subConstraints) {
        min += constraint.minArity();
        max += constraint.maxArity();
      }
      this.minArity = min;
      this.maxArity = max;
    }

    @Override
    public <T> void check(Adapter<T> adapter, Iterator<ExpressionOrValue<T>> arguments) {
      for (int i = 0; i < subConstraints.length; i++) {
        if (arguments.hasNext()) {
          subConstraints[i].check(adapter, arguments);
        } else {
          throw new InternalArityException();
        }
      }
    }

    @Override
    public int minArity() {
      return minArity;
    }

    @Override
    public int maxArity() {
      return maxArity;
    }

    @Override
    public String expectedType() {
      throw new IllegalStateException("A heterogenous list constraint does not have an expected type");
    }
  }

  private static abstract class TypeCheck implements ArgumentConstraint {
    @Override
    public <T> void check(Adapter<T> adapter, Iterator<ExpressionOrValue<T>> arguments) {
      if (arguments.hasNext()) {
        checkType(adapter, arguments.next());
      } else {
        throw new InternalArityException();
      }
    }

    protected abstract <T> void checkType(Adapter<T> adapter, ExpressionOrValue<T> argument);

    @Override
    public int minArity() {
      return 1;
    }

    @Override
    public int maxArity() {
      return 1;
    }
  }

  private static class AnyValue extends TypeCheck {
    @Override
    protected <T> void checkType(Adapter<T> adapter, ExpressionOrValue<T> argument) {
      if (argument.isExpression()) {
        throw new InternalArgumentTypeException("any value", "expression");
      }
    }

    @Override
    public String expectedType() {
      return "any value";
    }
  }

  private static class TypeOf extends TypeCheck {
    private final JmesPathType expectedType;

    public TypeOf(JmesPathType expectedType) {
      this.expectedType = expectedType;
    }

    @Override
    protected <T> void checkType(Adapter<T> adapter, ExpressionOrValue<T> argument) {
      if (argument.isExpression()) {
        throw new InternalArgumentTypeException(expectedType.toString(), "expression");
      } else {
        JmesPathType actualType = adapter.typeOf(argument.value());
        if (actualType != expectedType) {
          throw new InternalArgumentTypeException(expectedType.toString(), actualType.toString());
        }
      }
    }

    @Override
    public String expectedType() {
      return expectedType.toString();
    }
  }

  private static class TypeOfEither extends TypeCheck {
    private final JmesPathType[] expectedTypes;
    private final String expectedTypeString;

    public TypeOfEither(JmesPathType[] expectedTypes) {
      this.expectedTypes = expectedTypes;
      this.expectedTypeString = createExpectedTypeString(expectedTypes);
    }

    private String createExpectedTypeString(JmesPathType[] expectedTypes) {
      StringBuilder buffer = new StringBuilder();
      for (int i = 0; i < expectedTypes.length; i++) {
        buffer.append(expectedTypes[i]);
        if (i < expectedTypes.length - 2) {
          buffer.append(", ");
        } else if (i < expectedTypes.length - 1) {
          buffer.append(" or ");
        }
      }
      return buffer.toString();
    }

    @Override
    protected <T> void checkType(Adapter<T> adapter, ExpressionOrValue<T> argument) {
      if (argument.isExpression()) {
        throw new InternalArgumentTypeException(expectedTypeString, "expression");
      } else {
        JmesPathType actualType = adapter.typeOf(argument.value());
        for (int i = 0; i < expectedTypes.length; i++) {
          if (expectedTypes[i] == actualType) {
            return;
          }
        }
        throw new InternalArgumentTypeException(expectedTypeString, actualType.toString());
      }
    }

    @Override
    public String expectedType() {
      return expectedTypeString;
    }
  }

  private static class Expression extends TypeCheck {
    @Override
    protected <T> void checkType(Adapter<T> adapter, ExpressionOrValue<T> argument) {
      if (!argument.isExpression()) {
        throw new InternalArgumentTypeException("expression", adapter.typeOf(argument.value()).toString());
      }
    }

    @Override
    public int minArity() {
      return 1;
    }

    @Override
    public int maxArity() {
      return 1;
    }

    @Override
    public String expectedType() {
      return "expression";
    }
  }

  private static class ArrayOf implements ArgumentConstraint {
    private ArgumentConstraint subConstraint;

    public ArrayOf(ArgumentConstraint subConstraint) {
      this.subConstraint = subConstraint;
    }

    @Override
    public <T> void check(Adapter<T> adapter, Iterator<ExpressionOrValue<T>> arguments) {
      if (arguments.hasNext()) {
        ExpressionOrValue<T> argument = arguments.next();
        if (argument.isExpression()) {
          throw new InternalArgumentTypeException(expectedType(), "expression");
        } else {
          T value = argument.value();
          JmesPathType type = adapter.typeOf(value);
          if (type == JmesPathType.ARRAY) {
            checkElements(adapter, value);
          } else {
            throw new InternalArgumentTypeException(expectedType(), type.toString());
          }
        }
      } else {
        throw new InternalArityException();
      }
    }

    private <T> void checkElements(Adapter<T> adapter, T value) {
      List<T> elements = adapter.toList(value);
      if (!elements.isEmpty()) {
        List<ExpressionOrValue<T>> wrappedElements = new ArrayList<>(elements.size());
        Set<JmesPathType> types = new LinkedHashSet<>();
        for (T element : elements) {
          wrappedElements.add(new ExpressionOrValue<T>(element));
          types.add(adapter.typeOf(element));
        }
        if (types.size() > 1) {
          handleMixedError(types);
        }
        Iterator<ExpressionOrValue<T>> wrappedElementsIterator = wrappedElements.iterator();
        while (wrappedElementsIterator.hasNext()) {
          try {
            subConstraint.check(adapter, wrappedElementsIterator);
          } catch (InternalArgumentTypeException iate) {
            throw new InternalArgumentTypeException(expectedType(), String.format("array containing %s", iate.actualType()));
          }
        }
      }
    }

    private void handleMixedError(Set<JmesPathType> types) {
      StringBuilder actualTypes = new StringBuilder("array containing ");
      Object[] typesArray = types.toArray();
      for (int i = 0; i < typesArray.length; i++) {
        actualTypes.append(typesArray[i].toString());
        if (i < typesArray.length - 2) {
          actualTypes.append(", ");
        } else if (i < typesArray.length - 1) {
          actualTypes.append(" and ");
        }
      }
      throw new InternalArgumentTypeException(expectedType(), actualTypes.toString());
    }

    @Override
    public int minArity() {
      return 1;
    }

    @Override
    public int maxArity() {
      return 1;
    }

    @Override
    public String expectedType() {
      return String.format("array of %s", subConstraint.expectedType());
    }
  }
}
