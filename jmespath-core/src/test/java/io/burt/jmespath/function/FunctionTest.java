package io.burt.jmespath.function;

import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.ExpressionReferenceNode;
import io.burt.jmespath.node.PropertyNode;
import io.burt.jmespath.jcf.JcfRuntime;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class FunctionTest {
  private final Adapter<Object> runtime = new JcfRuntime();

  private final FunctionArgument<Object> expressionReference = FunctionArgument.of(
    new ExpressionReferenceNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo")
    )
  );

  private List<FunctionArgument<Object>> createValueArguments(Object... values) {
    List<FunctionArgument<Object>> arguments = new ArrayList<>();
    for (Object value : values) {
      arguments.add(FunctionArgument.of(value));
    }
    return arguments;
  }

  private static class TestFunction extends BaseFunction {
    public TestFunction(String name, ArgumentConstraint argumentConstraints) {
      super(name, argumentConstraints);
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
      return runtime.createNull();
    }
  }

  private static class BadName extends BaseFunction {
    public BadName() {
      super(ArgumentConstraints.anyValue());
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) { return null; }
  }

  private static class NameFromClassNameFunction extends BaseFunction {
    public NameFromClassNameFunction() {
      super(ArgumentConstraints.anyValue());
    }

    @Override
    protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
      return runtime.createNull();
    }
  }

  @Test
  public void nameMustEndWithFunction() {
    try {
      new BadName();
      fail("No exception thrown");
    } catch (FunctionConfigurationException fce) {
      assertThat(fce.getMessage(), containsString("must end with \"Function\""));
    }
  }

  @Test
  public void nameFromClassName() {
    assertThat(new NameFromClassNameFunction().name(), is("name_from_class_name"));
  }

  private Function heterogenousListOfFunction = new TestFunction(
    "heterogenous_list",
    ArgumentConstraints.listOf(
      ArgumentConstraints.typeOf(JmesPathType.NUMBER),
      ArgumentConstraints.typeOf(JmesPathType.STRING),
      ArgumentConstraints.typeOf(JmesPathType.BOOLEAN)
    )
  ) {};

  @Test
  public void heterogenousListOfRequiresEachArgumentToMatch() {
    heterogenousListOfFunction.call(runtime, createValueArguments(
      runtime.createNumber(1),
      runtime.createString("hello"),
      runtime.createBoolean(true)
    ));
    try {
      heterogenousListOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(1),
        runtime.createNumber(2),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Invalid argument type calling \"heterogenous_list\": expected string but was number"));
    }
  }

  @Test
  public void heterogenousListOfWithTooFewArguments() {
    try {
      heterogenousListOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(1),
        runtime.createString("hello")
      ));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), is("Invalid arity calling \"heterogenous_list\" (expected 3 but was 2)"));
    }
  }

  @Test
  public void heterogenousListOfWithTooManyArguments() {
    try {
      heterogenousListOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(1),
        runtime.createString("hello"),
        runtime.createBoolean(false),
        runtime.createNumber(4)
      ));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), is("Invalid arity calling \"heterogenous_list\" (expected 3 but was 4)"));
    }
  }

  private Function typeOfFunction = new TestFunction(
    "type_of",
    ArgumentConstraints.typeOf(JmesPathType.NUMBER)
  ) {};

  @Test
  public void typeOfRequiresTheArgumentToHaveTheRightType() {
    typeOfFunction.call(runtime, createValueArguments(
      runtime.createNumber(3)
    ));
    try {
      typeOfFunction.call(runtime, createValueArguments(
        runtime.createString("hello")
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"type_of\": expected number but was string"));
    }
  }

  @Test
  public void typeOfDoesNotAcceptExpressions() {
    try {
      typeOfFunction.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"type_of\": expected number but was expression"));
    }
  }

  @Test
  public void typeOfRequiresExactlyOneArgument() {
    try {
      typeOfFunction.call(runtime, createValueArguments());
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"type_of\" (expected 1 but was 0)"));
    }
    try {
      typeOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(3),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"type_of\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void typeOfWithMultipleTypesAcceptsEitherType() {
    Function wantsStringBooleanOrNumberFunction = new TestFunction(
      "wants_string_boolean_or_number",
      ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.BOOLEAN, JmesPathType.NUMBER)
    ) {};
    wantsStringBooleanOrNumberFunction.call(runtime, createValueArguments(runtime.createString("hello")));
    wantsStringBooleanOrNumberFunction.call(runtime, createValueArguments(runtime.createBoolean(true)));
    wantsStringBooleanOrNumberFunction.call(runtime, createValueArguments(runtime.createNumber(3)));
    try {
      wantsStringBooleanOrNumberFunction.call(runtime, createValueArguments(runtime.createNull()));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"wants_string_boolean_or_number\": expected string, boolean or number but was null"));
    }
  }

  private Function arrayOfFunction = new TestFunction(
    "array_of",
    ArgumentConstraints.arrayOf(
      ArgumentConstraints.typeOf(JmesPathType.STRING)
    )
  ) {};

  @Test
  public void arrayOfRequiresAnArray() {
    arrayOfFunction.call(runtime, createValueArguments(
      runtime.createArray(Arrays.asList(
      runtime.createString("hello"),
      runtime.createString("world")
      ))
    ));
    try {
      arrayOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Invalid argument type calling \"array_of\": expected array of string but was number"));
    }
  }

  @Test
  public void arrayOfRequiresTheArraysElementsToMatchTheSubConstraint() {
    arrayOfFunction.call(runtime, createValueArguments(
      runtime.createArray(Arrays.asList(
        runtime.createString("hello"),
        runtime.createString("world")
      ))
    ));
    try {
      arrayOfFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(
          runtime.createNumber(3)
        ))
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Invalid argument type calling \"array_of\": expected array of string but was array containing number"));
    }
    try {
      arrayOfFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(
          runtime.createString("foo"),
          runtime.createBoolean(true),
          runtime.createNumber(3)
        ))
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Invalid argument type calling \"array_of\": expected array of string but was array containing string, boolean and number"));
    }
  }

  @Test
  public void arrayOfDoesNotAcceptExpressions() {
    try {
      arrayOfFunction.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Invalid argument type calling \"array_of\": expected array of string but was expression"));
    }
  }

  @Test
  public void arrayOfAcceptsEmptyArray() {
    Function wantsStringOrNumberArrayFunction = new TestFunction(
      "wants_string_or_number_array",
      ArgumentConstraints.arrayOf(
        ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.NUMBER)
      )
    ) {};
    wantsStringOrNumberArrayFunction.call(runtime, createValueArguments(runtime.createArray(Arrays.asList())));
  }

  @Test
  public void arrayOfRequiresAllElementsToBeOfTheSameType() {
    Function wantsStringOrNumberArrayFunction = new TestFunction(
      "wants_string_or_number_array",
      ArgumentConstraints.arrayOf(
        ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.NUMBER)
      )
    ) {};
    try {
      wantsStringOrNumberArrayFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(
          runtime.createNumber(3),
          runtime.createString("hello")
        ))
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"wants_string_or_number_array\": expected array of string or number but was array containing number and string"));
    }
    try {
      wantsStringOrNumberArrayFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(
          runtime.createNumber(3),
          runtime.createString("hello"),
          runtime.createString("world"),
          runtime.createNull()
        ))
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"wants_string_or_number_array\": expected array of string or number but was array containing number, string and null"));
    }
  }

  @Test
  public void arrayOfRequiresExactlyOneArgument() {
    try {
      arrayOfFunction.call(runtime, createValueArguments());
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"array_of\" (expected 1 but was 0)"));
    }
    try {
      arrayOfFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(runtime.createString("hello"))),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"array_of\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void anyValueAcceptsAnyValue() {
    Function acceptsAnyValue = new TestFunction(
      "accepts_any_value",
      ArgumentConstraints.anyValue()
    ) {};
    acceptsAnyValue.call(runtime, createValueArguments(runtime.createNumber(3)));
    acceptsAnyValue.call(runtime, createValueArguments(runtime.createBoolean(false)));
    acceptsAnyValue.call(runtime, createValueArguments(runtime.createString("hello")));
    acceptsAnyValue.call(runtime, createValueArguments(runtime.createNull()));
    acceptsAnyValue.call(runtime, createValueArguments(runtime.createArray(Arrays.asList(runtime.createNull(), runtime.createBoolean(true)))));
  }

  @Test
  public void anyValueDoesNotAcceptExpressions() {
    Function doesNotAcceptExpression = new TestFunction(
      "does_not_accept_expression",
      ArgumentConstraints.anyValue()
    ) {};
    try {
      doesNotAcceptExpression.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"does_not_accept_expression\": expected any value but was expression"));
    }
  }

  @Test
  public void listOfAcceptsASpecifiedNumberOfValues() {
    Function acceptsBetweenThreeAndTenValues = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(3, 10, ArgumentConstraints.anyValue())
    ) {};
    acceptsBetweenThreeAndTenValues.call(runtime, createValueArguments(
      runtime.createNull(),
      runtime.createNumber(3),
      runtime.createString("hello"),
      runtime.createBoolean(false)
    ));
  }

  @Test
  public void listOfUsesASubConstraintToCheckEachArgument() {
    Function acceptsNumbers = new TestFunction(
      "accepts_numbers",
      ArgumentConstraints.listOf(3, 10, ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    ) {};
    acceptsNumbers.call(runtime, createValueArguments(
      runtime.createNumber(3),
      runtime.createNumber(3),
      runtime.createNumber(3),
      runtime.createNumber(3)
    ));
    try {
      acceptsNumbers.call(runtime, createValueArguments(
        runtime.createNumber(3),
        runtime.createNumber(3),
        runtime.createString("foobar"),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"accepts_numbers\": expected number but was string"));
    }
  }

  @Test
  public void listOfDoesNotAcceptExpressions() {
    Function doesNotAcceptExpression = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(1, 3, ArgumentConstraints.anyValue())
    ) {};
    try {
      List<FunctionArgument<Object>> arguments = createValueArguments(
        runtime.createNumber(3)
      );
      arguments.add(expressionReference);
      doesNotAcceptExpression.call(runtime, arguments);
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"hello\": expected any value but was expression"));
    }
  }

  @Test
  public void listOfNeedsTheMinimumAmountOfValues() {
    Function acceptsBetweenThreeAndTenValues = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(3, 10, ArgumentConstraints.anyValue())
    ) {};
    try {
      acceptsBetweenThreeAndTenValues.call(runtime, createValueArguments(runtime.createNull()));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"hello\" (expected at least 3 but was 1)"));
    }
  }

  @Test
  public void listOfAcceptsOnlyTheMaximumAmountOfValues() {
    Function acceptsBetweenThreeAndTenValues = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(1, 3, ArgumentConstraints.anyValue())
    ) {};
    try {
      acceptsBetweenThreeAndTenValues.call(runtime, createValueArguments(runtime.createNull(), runtime.createNull(), runtime.createNull(), runtime.createNull()));
      fail("No exception was thrown");
    } catch (IllegalStateException ise) {
      assertThat(ise.getMessage(), containsString("Invalid arity calling \"hello\" (expected at most 3 but was 4)"));
    }
  }

  @Test
  public void expressionAcceptsAnExpressionReference() {
    Function acceptsExpression = new TestFunction(
      "gief_expression",
      ArgumentConstraints.listOf(ArgumentConstraints.expression(), ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    ) {};
    acceptsExpression.call(runtime, Arrays.asList(
      expressionReference,
      FunctionArgument.of(runtime.createNumber(3))
    ));
  }

  @Test
  public void expressionDoesNotAcceptAValue() {
    Function acceptsExpression = new TestFunction(
      "gief_expression",
      ArgumentConstraints.listOf(ArgumentConstraints.expression(), ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    ) {};
    try {
      acceptsExpression.call(runtime, createValueArguments(
        runtime.createNumber(3),
        runtime.createNumber(4)
      ));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Invalid argument type calling \"gief_expression\": expected expression but was number"));
    }
  }
}
