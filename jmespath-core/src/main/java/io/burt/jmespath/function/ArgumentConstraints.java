package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
  private static final String EXPRESSION_TYPE = "expression";

  /**
   * Describes a heterogenous list of arguments. Each argument is checked against
   * the corresponding constraint. An {@link ArityException} will be thrown when
   * the number of arguments does not exactly match the number of constraints.
   * <p>
   * May only be used as a top level constraint – and is already built in to
   * {@link Function}, so direct usage of this method should not be needed.
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
   * one of the specified types (as determined by {@link Adapter#typeOf})
   * or is an expression.
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

  private ArgumentConstraints() {}

  private static abstract class BaseArgumentConstraint implements ArgumentConstraint {
    private final int minArity;
    private final int maxArity;
    private final String expectedTypeDescription;

    public BaseArgumentConstraint(int minArity, int maxArity, String expectedTypeDescription) {
      this.minArity = minArity;
      this.maxArity = maxArity;
      this.expectedTypeDescription = expectedTypeDescription;
    }

    protected <T> Iterator<ArgumentError> checkNoRemaingArguments(Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments) {
      if (expectNoRemainingArguments && arguments.hasNext()) {
        return singletonIterator(ArgumentError.createArityError());
      } else {
        return emptyIterator();
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
      return expectedTypeDescription;
    }

    protected <U> Iterator<U> singletonIterator(U obj) {
      return Collections.singleton(obj).iterator();
    }

    protected <U> Iterator<U> emptyIterator() {
      return Collections.emptyIterator();
    }
  }

  private static class HomogenousListOf extends BaseArgumentConstraint {
    private final ArgumentConstraint subConstraint;

    public HomogenousListOf(int minArity, int maxArity, ArgumentConstraint subConstraint) {
      super(minArity, maxArity, subConstraint.expectedType());
      this.subConstraint = subConstraint;
    }

    @Override
    public <T> Iterator<ArgumentError> check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments) {
      int i = 0;
      for (; i < minArity(); i++) {
        if (!arguments.hasNext()) {
          return singletonIterator(ArgumentError.createArityError());
        } else {
          Iterator<ArgumentError> error = subConstraint.check(runtime, arguments, false);
          if (error.hasNext()) {
            return error;
          }
        }
      }
      for (; i < maxArity(); i++) {
        if (arguments.hasNext()) {
          Iterator<ArgumentError> error = subConstraint.check(runtime, arguments, false);
          if (error.hasNext()) {
        	  return error;
          }
        } else {
          break;
        }
      }
      return checkNoRemaingArguments(arguments, expectNoRemainingArguments);
    }
  }

  private static class HeterogenousListOf extends BaseArgumentConstraint {
    private final ArgumentConstraint[] subConstraints;

    public HeterogenousListOf(ArgumentConstraint[] subConstraints) {
      super(calculateMinArity(subConstraints), calculateMaxArity(subConstraints), null);
      this.subConstraints = subConstraints;
    }

    private static int calculateMinArity(ArgumentConstraint[] subConstraints) {
      int min = 0;
      for (ArgumentConstraint constraint : subConstraints) {
        min += constraint.minArity();
      }
      return min;
    }

    private static int calculateMaxArity(ArgumentConstraint[] subConstraints) {
      int max = 0;
      for (ArgumentConstraint constraint : subConstraints) {
        max += constraint.maxArity();
      }
      return max;
    }

    @Override
    public <T> Iterator<ArgumentError> check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments) {
      for (int i = 0; i < subConstraints.length; i++) {
        if (arguments.hasNext()) {
          Iterator<ArgumentError> error = subConstraints[i].check(runtime, arguments, false);
          if (error.hasNext()) {
            return error;
          }
        } else {
          return singletonIterator(ArgumentError.createArityError());
        }
      }
      return checkNoRemaingArguments(arguments, expectNoRemainingArguments);
    }
  }

  private static abstract class TypeCheck extends BaseArgumentConstraint {
    public TypeCheck(String expectedType) {
      super(1, 1, expectedType);
    }

    @Override
    public <T> Iterator<ArgumentError> check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments) {
      if (arguments.hasNext()) {
        Iterator<ArgumentError> error = checkType(runtime, arguments.next());
        if (error.hasNext()) {
          return error;
        } else {
          return checkNoRemaingArguments(arguments, expectNoRemainingArguments);
        }
      } else {
        return singletonIterator(ArgumentError.createArityError());
      }
    }

    protected abstract <T> Iterator<ArgumentError> checkType(Adapter<T> runtime, FunctionArgument<T> argument);
  }

  private static class AnyValue extends TypeCheck {
    public AnyValue() {
      super("any value");
    }

    @Override
    protected <T> Iterator<ArgumentError> checkType(Adapter<T> runtime, FunctionArgument<T> argument) {
      if (argument.isExpression()) {
        return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError("any value", EXPRESSION_TYPE));
      } else {
        return emptyIterator();
      }
    }
  }

  private static class TypeOf extends TypeCheck {
    private final JmesPathType expectedType;

    public TypeOf(JmesPathType expectedType) {
      super(expectedType.toString());
      this.expectedType = expectedType;
    }

    @Override
    protected <T> Iterator<ArgumentError> checkType(Adapter<T> runtime, FunctionArgument<T> argument) {
      if (argument.isExpression()) {
        return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType.toString(), EXPRESSION_TYPE));
      } else {
        JmesPathType actualType = runtime.typeOf(argument.value());
        if (actualType != expectedType) {
          return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType.toString(), actualType.toString()));
        }
      }
      return emptyIterator();
    }
  }

  private static class TypeOfEither extends TypeCheck {
    private final JmesPathType[] expectedTypes;

    public TypeOfEither(JmesPathType[] expectedTypes) {
      super(createExpectedTypeString(expectedTypes));
      this.expectedTypes = expectedTypes;
    }

    private static String createExpectedTypeString(JmesPathType[] expectedTypes) {
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
    protected <T> Iterator<ArgumentError> checkType(Adapter<T> runtime, FunctionArgument<T> argument) {
      if (argument.isExpression()) {
        return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType(), EXPRESSION_TYPE));
      } else {
        JmesPathType actualType = runtime.typeOf(argument.value());
        for (int i = 0; i < expectedTypes.length; i++) {
          if (expectedTypes[i] == actualType) {
            return emptyIterator();
          }
        }
        return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType(), actualType.toString()));
      }
    }
  }

  private static class Expression extends TypeCheck {
    public Expression() {
      super(EXPRESSION_TYPE);
    }

    @Override
    protected <T> Iterator<ArgumentError> checkType(Adapter<T> runtime, FunctionArgument<T> argument) {
      if (!argument.isExpression()) {
        return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(EXPRESSION_TYPE, runtime.typeOf(argument.value()).toString()));
      } else {
        return emptyIterator();
      }
    }
  }

  private static class ArrayOf extends BaseArgumentConstraint {
    private ArgumentConstraint subConstraint;

    public ArrayOf(ArgumentConstraint subConstraint) {
      super(1, 1, String.format("array of %s", subConstraint.expectedType()));
      this.subConstraint = subConstraint;
    }

    @Override
    public <T> Iterator<ArgumentError> check(Adapter<T> runtime, Iterator<FunctionArgument<T>> arguments, boolean expectNoRemainingArguments) {
      if (arguments.hasNext()) {
        FunctionArgument<T> argument = arguments.next();
        if (argument.isExpression()) {
          return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType(), EXPRESSION_TYPE));
        } else {
          T value = argument.value();
          JmesPathType type = runtime.typeOf(value);
          if (type == JmesPathType.ARRAY) {
            Iterator<ArgumentError> error = checkElements(runtime, value);
            if (error.hasNext()) {
              return error;
            }
          } else {
            return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType(), type.toString()));
          }
        }
        return checkNoRemaingArguments(arguments, expectNoRemainingArguments);
      } else {
        return singletonIterator(ArgumentError.createArityError());
      }
    }

    private <T> Iterator<ArgumentError> checkElements(Adapter<T> runtime, T value) {
      List<T> elements = runtime.toList(value);
      if (!elements.isEmpty()) {
        List<FunctionArgument<T>> wrappedElements = new ArrayList<>(elements.size());
        Set<JmesPathType> types = new LinkedHashSet<>();
        for (T element : elements) {
          wrappedElements.add(FunctionArgument.of(element));
          types.add(runtime.typeOf(element));
        }
        if (types.size() > 1) {
          return singletonIterator(createMixedTypesError(types));
        }
        Iterator<FunctionArgument<T>> wrappedElementsIterator = wrappedElements.iterator();
        while (wrappedElementsIterator.hasNext()) {
          Iterator<ArgumentError> error = subConstraint.check(runtime, wrappedElementsIterator, false);
          if (error.hasNext()) {
            ArgumentError e = error.next();
            if (e instanceof ArgumentError.ArgumentTypeError) {
              ArgumentError.ArgumentTypeError ee = (ArgumentError.ArgumentTypeError) e;
              return singletonIterator((ArgumentError) ArgumentError.createArgumentTypeError(expectedType(), String.format("array containing %s", ee.actualType())));
            } else {
              return singletonIterator(e);
            }
          }
        }
      }
      return emptyIterator();
    }

    private ArgumentError createMixedTypesError(Set<JmesPathType> types) {
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
      return ArgumentError.createArgumentTypeError(expectedType(), actualTypes.toString());
    }
  }
}
