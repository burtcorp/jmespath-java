package io.burt.jmespath.parser;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.jcf.JcfRuntime;
import io.burt.jmespath.node.CreateObjectNode;
import io.burt.jmespath.node.Node;
import io.burt.jmespath.node.Operator;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.FunctionArgument;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class ParserTest {
  private Adapter<Object> runtime = new JcfRuntime();

  private Expression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private Node<Object> Current() {
    return runtime.nodeFactory().createCurrent();
  }

  private Node<Object> Property(String name) {
    return runtime.nodeFactory().createProperty(name);
  }

  private Node<Object> Index(int index) {
    return runtime.nodeFactory().createIndex(index);
  }

  private Node<Object> Slice(Integer start, Integer stop, Integer step) {
    return runtime.nodeFactory().createSlice(start, stop, step);
  }

  private Node<Object> Projection(Expression<Object> expression) {
    return runtime.nodeFactory().createProjection(expression);
  }

  private Node<Object> FlattenArray() {
    return runtime.nodeFactory().createFlattenArray();
  }

  private Node<Object> FlattenObject() {
    return runtime.nodeFactory().createFlattenObject();
  }

  private Node<Object> Selection(Expression<Object> test) {
    return runtime.nodeFactory().createSelection(test);
  }

  private Node<Object> Comparison(String operator, Expression<Object> left, Expression<Object> right) {
    return runtime.nodeFactory().createComparison(Operator.fromString(operator), left, right);
  }

  private Node<Object> Or(Expression<Object> left, Expression<Object> right) {
    return runtime.nodeFactory().createOr(left, right);
  }

  private Node<Object> And(Expression<Object> left, Expression<Object> right) {
    return runtime.nodeFactory().createAnd(left, right);
  }

  private Node<Object> FunctionCall(String functionName, List<? extends Expression<Object>> args) {
    return runtime.nodeFactory().createFunctionCall(functionName, args);
  }

  private Node<Object> ExpressionReference(Expression<Object> expression) {
    return runtime.nodeFactory().createExpressionReference(expression);
  }

  private Node<Object> String(String str) {
    return runtime.nodeFactory().createString(str);
  }

  private Node<Object> Negate(Node<Object> negated) {
    return runtime.nodeFactory().createNegate(negated);
  }

  private Node<Object> Object(List<CreateObjectNode.Entry<Object>> entries) {
    return runtime.nodeFactory().createCreateObject(entries);
  }

  private Node<Object> Array(List<? extends Expression<Object>> entries) {
    return runtime.nodeFactory().createCreateArray(entries);
  }

  private Node<Object> JsonLiteral(String json) {
    return runtime.nodeFactory().createJsonLiteral(json);
  }

  private Node<Object> Sequence(Node<Object> first, Node<Object> second) {
    return runtime.nodeFactory().createSequence(Arrays.asList(first, second));
  }

  @Test
  public void identifierExpression() {
    Expression<Object> expected = Property("foo");
    Expression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Expression<Object> expected = Property("foo-bar");
    Expression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test()
  public void quotedIdentifierExpressionsAreUnescapedLikeJsonStrings() {
    Expression<Object> expected = Property("\\foo bar\n");
    Expression<Object> actual = compile("\"\\\\foo\\u0020bar\\n\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Property("bar")
    );
    Expression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Sequence(
        Property("bar"),
        Sequence(
          Property("baz"),
          Property("qux")
        )
      )
    );
    Expression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Property("bar")
    );
    Expression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    Expression<Object> expected = Sequence(
      Sequence(
        Sequence(
          Property("foo"),
          Property("bar")
        ),
        Property("baz")
      ),
      Property("qux")
    );
    Expression<Object> actual = compile("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("foo"),
        Property("bar")
      ),
      Sequence(
        Property("baz"),
        Property("qux")
      )
    );
    Expression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Index(3)
    );
    Expression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Expression<Object> expected = Index(3);
    Expression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(3, 4, 1)
    );
    Expression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(3, null, 1)
    );
    Expression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(null, 4, 1)
    );
    Expression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(3, 4, 5)
    );
    Expression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(3, null, 5)
    );
    Expression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(null, null, 1)
    );
    Expression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(null, null, 1)
    );
    Expression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Expression<Object> expected = Slice(0, 1, 2);
    Expression<Object> actual = compile("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithZeroStepSize() {
    try {
      compile("[0:1:0]");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Unable to compile expression \"[0:1:0]\": invalid value 0 for step size at position 5"));
    }
  }

  @Test
  public void flattenExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      FlattenArray()
    );
    Expression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Expression<Object> expected = FlattenArray();
    Expression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Projection(Current())
    );
    Expression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Expression<Object> expected = Projection(Current());
    Expression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      FlattenObject()
    );
    Expression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Expression<Object> expected = FlattenObject();
    Expression<Object> actual = compile("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Expression<Object> expected = Current();
    Expression<Object> actual = compile("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    Expression<Object> expected = Sequence(
      Sequence(
        Sequence(
          Sequence(
            Current(),
            Property("foo")
          ),
          Current()
        ),
        Property("bar")
      ),
      Current()
    );
    Expression<Object> actual = compile("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Property("bar")
      )
    );
    Expression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Comparison("==",
          Property("bar"),
          Property("baz")
        )
      )
    );
    Expression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Expression<Object> expected = Selection(
      Property("bar")
    );
    Expression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(Current())
    );
    Expression<Object> actual = compile("sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(Property("bar"))
    );
    Expression<Object> actual = compile("sort(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Expression<Object> expected = FunctionCall("merge",
      Arrays.asList(
        Property("bar"),
        Property("baz"),
        Current()
      )
    );
    Expression<Object> actual = compile("merge(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      FunctionCall("to_string",
        Arrays.asList(Current())
      )
    );
    Expression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(
        ExpressionReference(
          Sequence(
            Property("bar"),
            Property("bar")
          )
        )
      )
    );
    Expression<Object> actual = compile("sort(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithUnknownFunction() {
    try {
      compile("to_unicorn(@)");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Unable to compile expression \"to_unicorn(@)\": unknown function \"to_unicorn\" at position 0"));
    }
  }

  @Test
  public void bareRawStringExpression() {
    Expression<Object> expected = String("foo");
    Expression<Object> actual = compile("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Comparison("!=",
          Property("bar"),
          String("baz")
        )
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Expression<Object> expected = And(
      Property("foo"),
      Property("bar")
    );
    Expression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Expression<Object> expected = Or(
      Property("foo"),
      Property("bar")
    );
    Expression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Projection(
        Current()
      )
    );
    Expression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Index(1)
    );
    Expression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Slice(1, 2, 1)
    );
    Expression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      FlattenArray()
    );
    Expression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Property("bar")
      )
    );
    Expression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Or(
          And(
            Comparison("!=",
              Property("bar"),
              String("baz")
            ),
            Comparison("==",
              Property("qux"),
              String("fux")
            )
          ),
          Comparison(">",
            Property("mux"),
            String("lux")
          )
        )
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Expression<Object> expected = Sequence(
      Sequence(
        Sequence(
          Property("foo"),
          Property("bar")
        ),
        FlattenArray()
      ),
      FunctionCall("sort",
        Arrays.asList(Current())
      )
    );
    Expression<Object> actual = compile("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("foo"),
        Sequence(
          Index(3),
          Property("bar")
        )
      ),
      Sequence(
        Property("baz"),
        Sequence(
          Property("qux"),
          Slice(2, 3, 1)
        )
      )
    );
    Expression<Object> actual = compile("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectHashExpression() {
    Expression<Object> expected = Object(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", String("bar")),
        new CreateObjectNode.Entry<Object>("baz", Current())
      )
    );
    Expression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    Expression<Object> expected = Sequence(
      Property("hello"),
      Sequence(
        Property("world"),
        Object(
          Arrays.asList(
            new CreateObjectNode.Entry<Object>("foo", String("bar")),
            new CreateObjectNode.Entry<Object>("baz", Current())
          )
        )
      )
    );
    Expression<Object> actual = compile("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashWithQuotedKeys() {
    Expression<Object> expected = Object(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", String("bar")),
        new CreateObjectNode.Entry<Object>("baz", Current())
      )
    );
    Expression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    Expression<Object> expected = Sequence(
      Sequence(
        Sequence(
          Property("locations"),
          Sequence(
            Selection(
              Comparison("==",
                Property("state"),
                String("WA")
              )
            ),
            Projection(
              Property("name")
            )
          )
        ),
        FunctionCall("sort",
          Arrays.asList(
            Current()
          )
        )
      ),
      Object(
        Arrays.asList(
          new CreateObjectNode.Entry<Object>("WashingtonCities",
            FunctionCall("join",
              Arrays.asList(
                String(", "),
                Current()
              )
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    Expression<Object> expected = Array(
      Arrays.asList(
        String("bar"),
        Current()
      )
    );
    Expression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Expression<Object> expected = Sequence(
      Property("hello"),
      Sequence(
        Property("world"),
        Array(
          Arrays.asList(
            String("bar"),
            Current()
          )
        )
      )
    );
    Expression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Sequence(
        Property("bar"),
        Property("baz")
      )
    );
    Expression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        And(
          Comparison("==",
            Property("bar"),
            String("baz")
          ),
          Or(
            Comparison("==",
              Property("qux"),
              String("fux")
            ),
            Comparison("==",
              Property("mux"),
              String("lux")
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Expression<Object> expected = Negate(
      Property("foo")
    );
    Expression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Negate(Property("bar"))
      )
    );
    Expression<Object> actual = compile("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    Expression<Object> expected = JsonLiteral("{}");
    Expression<Object> actual = compile("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    Expression<Object> expected = JsonLiteral("[]");
    Expression<Object> actual = compile("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    Expression<Object> expected = JsonLiteral("3.14");
    Expression<Object> actual = compile("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    Expression<Object> expected = JsonLiteral("\"foo\"");
    Expression<Object> actual = compile("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    Expression<Object> expected = JsonLiteral("false");
    Expression<Object> actual = compile("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedNewlineAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"hello\\nworld\"");
    Expression<Object> actual = compile("`\"hello\\nworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedTabsAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"hello\\tworld\"");
    Expression<Object> actual = compile("`\"hello\\tworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedUnicodeCharsAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"hello\\u0020world\"");
    Expression<Object> actual = compile("`\"hello\\u0020world\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedQuotesAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"hello \\\"world\\\"\"");
    Expression<Object> actual = compile("`\"hello \\\"world\\\"\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedBackslashesAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"c:\\\\\\\\windows\\\\path\"");
    Expression<Object> actual = compile("`\"c:\\\\\\\\windows\\\\path\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithEscapedBackticksAreAllowed() {
    Expression<Object> expected = JsonLiteral("\"fo`o\"");
    Expression<Object> actual = compile("`\"fo\\`o\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedNewlinesAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"hello\\nworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\nworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedTabsAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"hello\\tworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\tworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedUnicodeCharsAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"hello\\u0020world\":1}");
    Expression<Object> actual = compile("`{\"hello\\u0020world\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedQuotesAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"hello \\\"world\\\"\":1}");
    Expression<Object> actual = compile("`{\"hello \\\"world\\\"\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedBackslashesAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"c:\\\\\\\\windows\\\\path\":1}");
    Expression<Object> actual = compile("`{\"c:\\\\\\\\windows\\\\path\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralObjectKeysWithEscapedBackticksAreAllowed() {
    Expression<Object> expected = JsonLiteral("{\"fo`o\":1}");
    Expression<Object> actual = compile("`{\"fo\\`o\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonLiteralStringsWithNonEscapedBackticksAreNotAllowed() {
    try {
      compile("`\"fo`o\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Unable to compile expression \"`\"fo`o\"`\": syntax error unexpected ` at position 4"));
    }
    try {
      compile("`\"`foo\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Unable to compile expression \"`\"`foo\"`\": syntax error unexpected ` at position 2"));
    }
  }

  @Test
  public void comparisonWithJsonLiteralExpression() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Selection(
        Comparison("==",
          Property("bar"),
          JsonLiteral("{\"foo\":\"bar\"}")
        )
      )
    );
    Expression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonBuiltinsAsNames() {
    Expression<Object> expected = Sequence(
      Property("false"),
      Sequence(
        Property("null"),
        Property("true")
      )
    );
    Expression<Object> actual = compile("false.null.true");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapesInRawStringsArePreserved() {
    Expression<Object> expected = String("\\u03a6hello\\nworld\\t");
    Expression<Object> actual = compile("'\\u03a6hello\\nworld\\t'");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleQuotesNeedsToBeEscapedInRawStrings() {
    Expression<Object> expected = String("'");
    Expression<Object> actual = compile("'\\''");
    assertThat(actual, is(expected));
  }

  @Test
  public void backslashesMustBeEscapedInRawStrings() {
    Expression<Object> expected = String("\\");
    Expression<Object> actual = compile("'\\\\'");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleLevelProjection() {
    Expression<Object> expected = Sequence(
      Property("foo"),
      Sequence(
        FlattenObject(),
        Projection(
          Property("bar")
        )
      )
    );
    Expression<Object> actual = compile("foo.*.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleLevelProjectionWithPipe() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("foo"),
        Sequence(
          FlattenObject(),
          Projection(
            Property("bar")
          )
        )
      ),
      Property("baz")
    );
    Expression<Object> actual = compile("foo.*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void multipleLevelsOfProjections() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("foo"),
        Sequence(
          Slice(null, null, null),
          Projection(
            Sequence(
              FlattenObject(),
              Projection(
                Property("bar")
              )
            )
          )
        )
      ),
      Property("baz")
    );
    Expression<Object> actual = compile("foo[:].*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation1() {
    Expression<Object> expected = Sequence(
      Property("Records"),
      Projection(
        Sequence(
          Property("userIdentity"),
          Array(
            Arrays.asList(
              Property("userName"),
              Sequence(
                Property("sessionContext"),
                Sequence(
                  Property("attributes"),
                  Property("mfaAuthenticated")
                )
              )
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation2() {
    Expression<Object> expected = Sequence(
      Property("Records"),
      Projection(
        Sequence(
          Property("requestParameters"),
          Property("keyName")
        )
      )
    );
    Expression<Object> actual = compile("Records[*].requestParameters.keyName");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAndFlatten() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("Records"),
        Sequence(
          Index(0),
          Sequence(
            Property("responseElements"),
            Sequence(
              FlattenObject(),
              Projection(
                Property("items")
              )
            )
          )
        )
      ),
      Sequence(
        FlattenArray(),
        Projection(
          Property("instanceId")
        )
      )
    );
    Expression<Object> actual = compile("Records[0].responseElements.*.items[].instanceId");
    assertThat(actual, is(expected));
  }

  @Test
  public void operationsAfterPipeAfterProjection() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("Records"),
        Selection(
          String("")
        )
      ),
      Negate(
        Current()
      )
    );
    Expression<Object> actual = compile("Records[?''] | !@");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedParenthesis() {
    Expression<Object> expected = Sequence(
      Sequence(
        Property("foo"),
        FlattenArray()
      ),
      Property("bar")
    );
    Expression<Object> actual = compile("(foo[]).bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void parseExceptionsCanBeIterated() {
    try {
      compile("foo`bar ^ hello");
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      int errorCount = 0;
      for (ParseError e : pe) {
        errorCount++;
      }
      assertThat(errorCount, is(2));
    }
  }

  @Test
  public void callingAFixedArityFunctionWithTooFewArgumentsThrowsParseException() {
    try {
      compile("max()");
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"max\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void callingAFixedArityFunctionWithTooManyArgumentsThrowsParseException() {
    try {
      compile("max(@, @)");
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"max\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void callingAVariableArityFunctionWithTooFewArgumentsThrowsParseException() {
    try {
      compile("not_null()");
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"not_null\" (expected at least 1 but was 0)"));
    }
  }

  @Test
  public void callingAVariableArityFunctionWithTooManyArgumentsThrowsParseException() {
    runtime = new JcfRuntime(RuntimeConfiguration.builder().withFunctionRegistry(FunctionRegistry.defaultRegistry().extend(
      new BaseFunction("foobar", ArgumentConstraints.listOf(1, 3, ArgumentConstraints.anyValue())) {
        protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) { return runtime.createNull(); }
      }
    )).build());
    try {
      compile("foobar(a, b, c, d, e, f, g)");
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"foobar\" (expected at most 3 but was 7)"));
    }
  }
}
