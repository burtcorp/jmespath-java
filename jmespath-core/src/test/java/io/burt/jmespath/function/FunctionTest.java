package io.burt.jmespath.function;

import org.junit.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import io.burt.jmespath.JmesPathRuntime;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.node.ExpressionReferenceNode;
import io.burt.jmespath.node.PropertyNode;
import io.burt.jmespath.node.CurrentNode;
import io.burt.jmespath.jcf.JcfRuntime;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class FunctionTest {
  private final JmesPathRuntime<Object> runtime = new JcfRuntime();

  private final ExpressionOrValue<Object> expressionReference = new ExpressionOrValue<Object>(new ExpressionReferenceNode(new PropertyNode("foo", new CurrentNode())));

  private List<ExpressionOrValue<Object>> createValueArguments(Object... values) {
    List<ExpressionOrValue<Object>> arguments = new ArrayList<>();
    for (Object value : values) {
      arguments.add(new ExpressionOrValue<Object>(value));
    }
    return arguments;
  }

  private static class TestFunction extends JmesPathFunction {
    public TestFunction(String name, ArgumentConstraint argumentConstraints) {
      super(name, argumentConstraints);
    }

    @Override
    protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
      return runtime.createNull();
    }
  }

  private static class BadName extends JmesPathFunction {
    public BadName() {
      super(ArgumentConstraints.anyValue());
    }

    @Override
    protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  private static class NameFromClassNameFunction extends JmesPathFunction {
    public NameFromClassNameFunction() {
      super(ArgumentConstraints.anyValue());
    }

    @Override
    protected <T> T callFunction(JmesPathRuntime<T> runtime, List<ExpressionOrValue<T>> arguments) {
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

  private JmesPathFunction heterogenousListOfFunction = new TestFunction(
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
      assertThat(ate.getMessage(), is("Wrong type of argument calling heterogenous_list: expected string but was number"));
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
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), is("Wrong number of arguments calling heterogenous_list: expected 3 but was 2"));
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
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), is("Wrong number of arguments calling heterogenous_list: expected 3 but was 4"));
    }
  }

  private JmesPathFunction typeOfFunction = new TestFunction(
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling type_of: expected number but was string"));
    }
  }

  @Test
  public void typeOfDoesNotAcceptExpressions() {
    try {
      typeOfFunction.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling type_of: expected number but was expression"));
    }
  }

  @Test
  public void typeOfRequiresExactlyOneArgument() {
    try {
      typeOfFunction.call(runtime, createValueArguments());
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling type_of: expected 1 but was 0"));
    }
    try {
      typeOfFunction.call(runtime, createValueArguments(
        runtime.createNumber(3),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling type_of: expected 1 but was 2"));
    }
  }

  @Test
  public void typeOfWithMultipleTypesAcceptsEitherType() {
    JmesPathFunction wantsStringBooleanOrNumberFunction = new TestFunction(
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling wants_string_boolean_or_number: expected string, boolean or number but was null"));
    }
  }

  private JmesPathFunction arrayOfFunction = new TestFunction(
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
      assertThat(ate.getMessage(), is("Wrong type of argument calling array_of: expected array of string but was number"));
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
      assertThat(ate.getMessage(), is("Wrong type of argument calling array_of: expected array of string but was array containing number"));
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
      assertThat(ate.getMessage(), is("Wrong type of argument calling array_of: expected array of string but was array containing string, boolean and number"));
    }
  }

  @Test
  public void arrayOfDoesNotAcceptExpressions() {
    try {
      arrayOfFunction.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), is("Wrong type of argument calling array_of: expected array of string but was expression"));
    }
  }

  @Test
  public void arrayOfAcceptsEmptyArray() {
    JmesPathFunction wantsStringOrNumberArrayFunction = new TestFunction(
      "wants_string_or_number_array",
      ArgumentConstraints.arrayOf(
        ArgumentConstraints.typeOf(JmesPathType.STRING, JmesPathType.NUMBER)
      )
    ) {};
    wantsStringOrNumberArrayFunction.call(runtime, createValueArguments(runtime.createArray(Arrays.asList())));
  }

  @Test
  public void arrayOfRequiresAllElementsToBeOfTheSameType() {
    JmesPathFunction wantsStringOrNumberArrayFunction = new TestFunction(
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling wants_string_or_number_array: expected array of string or number but was array containing number and string"));
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling wants_string_or_number_array: expected array of string or number but was array containing number, string and null"));
    }
  }

  @Test
  public void arrayOfRequiresExactlyOneArgument() {
    try {
      arrayOfFunction.call(runtime, createValueArguments());
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling array_of: expected 1 but was 0"));
    }
    try {
      arrayOfFunction.call(runtime, createValueArguments(
        runtime.createArray(Arrays.asList(runtime.createString("hello"))),
        runtime.createNumber(3)
      ));
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling array_of: expected 1 but was 2"));
    }
  }

  @Test
  public void anyValueAcceptsAnyValue() {
    JmesPathFunction acceptsAnyValue = new TestFunction(
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
    JmesPathFunction doesNotAcceptExpression = new TestFunction(
      "does_not_accept_expression",
      ArgumentConstraints.anyValue()
    ) {};
    try {
      doesNotAcceptExpression.call(runtime, Arrays.asList(expressionReference));
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling does_not_accept_expression: expected any value but was expression"));
    }
  }

  @Test
  public void listOfAcceptsASpecifiedNumberOfValues() {
    JmesPathFunction acceptsBetweenThreeAndTenValues = new TestFunction(
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
    JmesPathFunction acceptsNumbers = new TestFunction(
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling accepts_numbers: expected number but was string"));
    }
  }

  @Test
  public void listOfDoesNotAcceptExpressions() {
    JmesPathFunction doesNotAcceptExpression = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(1, 3, ArgumentConstraints.anyValue())
    ) {};
    try {
      List<ExpressionOrValue<Object>> arguments = createValueArguments(
        runtime.createNumber(3)
      );
      arguments.add(expressionReference);
      doesNotAcceptExpression.call(runtime, arguments);
      fail("No exception was thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling hello: expected any value but was expression"));
    }
  }

  @Test
  public void listOfNeedsTheMinimumAmountOfValues() {
    JmesPathFunction acceptsBetweenThreeAndTenValues = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(3, 10, ArgumentConstraints.anyValue())
    ) {};
    try {
      acceptsBetweenThreeAndTenValues.call(runtime, createValueArguments(runtime.createNull()));
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling hello: expected at least 3 but was 1"));
    }
  }

  @Test
  public void listOfAcceptsOnlyTheMaximumAmountOfValues() {
    JmesPathFunction acceptsBetweenThreeAndTenValues = new TestFunction(
      "hello",
      ArgumentConstraints.listOf(1, 3, ArgumentConstraints.anyValue())
    ) {};
    try {
      acceptsBetweenThreeAndTenValues.call(runtime, createValueArguments(runtime.createNull(), runtime.createNull(), runtime.createNull(), runtime.createNull()));
      fail("No exception was thrown");
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling hello: expected at most 3 but was 4"));
    }
  }

  @Test
  public void expressionAcceptsAnExpressionReference() {
    JmesPathFunction acceptsExpression = new TestFunction(
      "gief_expression",
      ArgumentConstraints.listOf(ArgumentConstraints.expression(), ArgumentConstraints.typeOf(JmesPathType.NUMBER))
    ) {};
    acceptsExpression.call(runtime, Arrays.asList(
      expressionReference,
      new ExpressionOrValue<Object>(runtime.createNumber(3))
    ));
  }

  @Test
  public void expressionDoesNotAcceptAValue() {
    JmesPathFunction acceptsExpression = new TestFunction(
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
      assertThat(ate.getMessage(), containsString("Wrong type of argument calling gief_expression: expected expression but was number"));
    }
  }
}
