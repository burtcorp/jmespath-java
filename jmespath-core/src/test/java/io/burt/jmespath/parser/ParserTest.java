package io.burt.jmespath.parser;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.Ignore;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jcf.JcfRuntime;
import io.burt.jmespath.node.AndNode;
import io.burt.jmespath.node.ComparisonNode;
import io.burt.jmespath.node.CreateArrayNode;
import io.burt.jmespath.node.CreateObjectNode;
import io.burt.jmespath.node.CurrentNode;
import io.burt.jmespath.node.ExpressionReferenceNode;
import io.burt.jmespath.node.FlattenArrayNode;
import io.burt.jmespath.node.FlattenObjectNode;
import io.burt.jmespath.node.ProjectionNode;
import io.burt.jmespath.node.FunctionCallNode;
import io.burt.jmespath.node.IndexNode;
import io.burt.jmespath.node.Node;
import io.burt.jmespath.node.JsonLiteralNode;
import io.burt.jmespath.node.NegateNode;
import io.burt.jmespath.node.OrNode;
import io.burt.jmespath.node.PropertyNode;
import io.burt.jmespath.node.SelectionNode;
import io.burt.jmespath.node.SliceNode;
import io.burt.jmespath.node.StringNode;
import io.burt.jmespath.node.Operator;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;

public class ParserTest {
  private Adapter<Object> runtime = new JcfRuntime();

  private Expression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private Node<Object> Current() {
    return runtime.nodeFactory().createCurrent();
  }

  private Node<Object> Current(Node<Object> source) {
    return runtime.nodeFactory().createCurrent(source);
  }

  private Node<Object> Property(String name, Node<Object> source) {
    return runtime.nodeFactory().createProperty(name, source);
  }

  private Node<Object> Index(int index, Node<Object> source) {
    return runtime.nodeFactory().createIndex(index, source);
  }

  private Node<Object> Slice(Integer start, Integer stop, Integer step, Node<Object> source) {
    return runtime.nodeFactory().createSlice(start, stop, step, source);
  }

  private Node<Object> Projection(Expression<Object> expression, Node<Object> source) {
    return runtime.nodeFactory().createProjection(expression, source);
  }

  private Node<Object> FlattenArray(Node<Object> source) {
    return runtime.nodeFactory().createFlattenArray(source);
  }

  private Node<Object> FlattenObject(Node<Object> source) {
    return runtime.nodeFactory().createFlattenObject(source);
  }

  private Node<Object> Selection(Expression<Object> test, Node<Object> source) {
    return runtime.nodeFactory().createSelection(test, source);
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

  private Node<Object> FunctionCall(String functionName, List<? extends Expression<Object>> args, Node<Object> source) {
    return runtime.nodeFactory().createFunctionCall(functionName, args, source);
  }

  private Node<Object> ExpressionReference(Expression<Object> expression) {
    return runtime.nodeFactory().createExpressionReference(expression);
  }

  private Node<Object> String(String str) {
    return runtime.nodeFactory().createString(str);
  }

  private Node<Object> Negate(Node<Object> source) {
    return runtime.nodeFactory().createNegate(source);
  }

  private Node<Object> Object(List<CreateObjectNode.Entry<Object>> entries, Node<Object> source) {
    return runtime.nodeFactory().createCreateObject(entries, source);
  }

  private Node<Object> Array(List<? extends Expression<Object>> entries, Node<Object> source) {
    return runtime.nodeFactory().createCreateArray(entries, source);
  }

  private Node<Object> JsonLiteral(String json) {
    return runtime.nodeFactory().createJsonLiteral(json);
  }

  @Test
  public void identifierExpression() {
    Expression<Object> expected = Property("foo", Current());
    Expression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Expression<Object> expected = Property("foo-bar", Current());
    Expression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test()
  public void quotedIdentifierExpressionsAreUnescapedLikeJsonStrings() {
    Expression<Object> expected = Property("\\foo bar\n", Current());
    Expression<Object> actual = compile("\"\\\\foo\\u0020bar\\n\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Expression<Object> expected = Property("bar",
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Expression<Object> expected = Property("qux",
      Property("baz",
        Property("bar",
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Expression<Object> expected = Property("bar",
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    Expression<Object> expected = Property("qux",
      Property("baz",
        Property("bar",
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Expression<Object> expected = Property("qux",
      Property("baz",
        Property("bar",
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Expression<Object> expected = Index(3,
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Expression<Object> expected = Index(3, Current());
    Expression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(3, 4, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(3, null, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(null, 4, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(3, 4, 5,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(3, null, 5,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(null, null, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(null, null, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(0, 1, 2, Current())
    );
    Expression<Object> actual = compile("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithZeroStepSize() {
    compile("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    Expression<Object> expected = Projection(
      Current(),
      FlattenArray(
          Property("foo", Current())
        )
      );
    Expression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Expression<Object> expected = Projection(
      Current(),
      FlattenArray( Current())
    );
    Expression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Expression<Object> expected = Projection(Current(), Current());
    Expression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Expression<Object> expected = Projection(
      Current(),
      FlattenObject(
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Expression<Object> expected = Projection(
      Current(),
      FlattenObject( Current())
    );
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
    Expression<Object> expected = Current(
      Property("bar",
        Current(
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Property("bar", Current()),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Comparison("==",
          Property("bar", Current()),
          Property("baz", Current())
        ),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Property("bar", Current()),
        Current()
      )
    );
    Expression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(Current()),
      Current()
    );
    Expression<Object> actual = compile("sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(Property("bar", Current())),
      Current()
    );
    Expression<Object> actual = compile("sort(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Expression<Object> expected = FunctionCall("merge",
      Arrays.asList(
        Property("bar", Current()),
        Property("baz", Current()),
        Current()
      ),
      Current()
    );
    Expression<Object> actual = compile("merge(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Expression<Object> expected = FunctionCall("to_string",
      Arrays.asList(Current()),
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(
        ExpressionReference(
          Property("bar",
            Property("bar", Current())
          )
        )
      ),
      Current()
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
      assertThat(pe.getMessage(), is("Error while parsing \"to_unicorn(@)\": unknown function \"to_unicorn\" at position 0"));
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
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Comparison("!=",
          Property("bar", Current()),
          String("baz")
        ),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Expression<Object> expected = And(
      Property("foo", Current()),
      Property("bar", Current())
    );
    Expression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Expression<Object> expected = Or(
      Property("foo", Current()),
      Property("bar", Current())
    );
    Expression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Expression<Object> expected = Projection(
      Current(),
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Expression<Object> expected = Index(1,
      Property("foo", Current())
    );
    Expression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(1, 2, 1,
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Expression<Object> expected = Projection(
      Current(),
      FlattenArray(
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Property("bar", Current()),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Or(
          And(
            Comparison("!=",
              Property("bar", Current()),
              String("baz")
            ),
            Comparison("==",
              Property("qux", Current()),
              String("fux")
            )
          ),
          Comparison(">",
            Property("mux", Current()),
            String("lux")
          )
        ),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Expression<Object> expected = FunctionCall("sort",
      Arrays.asList(Current()),
      Projection(
        Current(),
        FlattenArray(
          Property("bar",
            Property("foo", Current())
          )
        )
      )
    );
    Expression<Object> actual = compile("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Expression<Object> expected = Projection(
      Current(),
      Slice(2, 3, 1,
        Property("qux",
          Property("baz",
            Property("bar",
              Index(3,
                Property("foo", Current())
              )
            )
          )
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
      ),
      Current()
    );
    Expression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    Expression<Object> expected = Object(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", String("bar")),
        new CreateObjectNode.Entry<Object>("baz", Current())
      ),
      Property("world",
        Property("hello", Current())
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
      ),
      Current()
    );
    Expression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    Expression<Object> expected = Object(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("WashingtonCities",
          FunctionCall("join",
            Arrays.asList(
              String(", "),
              Current()
            ),
            Current()
          )
        )
      ),
      FunctionCall("sort",
        Arrays.asList(Current()),
        Projection(
          Property("name", Current()),
          Selection(
            Comparison("==",
              Property("state", Current()),
              String("WA")
            ),
            Property("locations", Current())
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
      ),
      Current()
    );
    Expression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Expression<Object> expected = Array(
      Arrays.asList(
        String("bar"),
        Current()
      ),
      Property("world",
        Property("hello", Current())
      )
    );
    Expression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Expression<Object> expected = Property("baz",
      Property("bar",
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        And(
          Comparison("==",
            Property("bar", Current()),
            String("baz")
          ),
          Or(
            Comparison("==",
              Property("qux", Current()),
              String("fux")
            ),
            Comparison("==",
              Property("mux", Current()),
              String("lux")
            )
          )
        ),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Expression<Object> expected = Negate(
      Property("foo", Current())
    );
    Expression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Negate(Property("bar", Current())),
        Property("foo", Current())
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
  public void bareJsonLiteralStringWithEscapedNewline() {
    Expression<Object> expected = JsonLiteral("\"hello\nworld\"");
    Expression<Object> actual = compile("`\"hello\\nworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedTab() {
    Expression<Object> expected = JsonLiteral("\"hello\tworld\"");
    Expression<Object> actual = compile("`\"hello\\tworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedUnicode() {
    Expression<Object> expected = JsonLiteral("\"hello\\u0020world\"");
    Expression<Object> actual = compile("`\"hello world\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedQuote() {
    Expression<Object> expected = JsonLiteral("\"hello \\\"world\\\"\"");
    Expression<Object> actual = compile("`\"hello \\\"world\\\"\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedBackslash() {
    Expression<Object> expected = JsonLiteral("\"c:\\\\\\\\windows\\\\path\"");
    Expression<Object> actual = compile("`\"c:\\\\\\\\windows\\\\path\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedNewlineInKey() {
    Expression<Object> expected = JsonLiteral("{\"hello\nworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\nworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedTabInKey() {
    Expression<Object> expected = JsonLiteral("{\"hello\tworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\tworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedUnicodeInKey() {
    Expression<Object> expected = JsonLiteral("{\"hello\\u0020world\":1}");
    Expression<Object> actual = compile("`{\"hello world\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedQuoteInKey() {
    Expression<Object> expected = JsonLiteral("{\"hello \\\"world\\\"\":1}");
    Expression<Object> actual = compile("`{\"hello \\\"world\\\"\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedBackslashInKey() {
    Expression<Object> expected = JsonLiteral("{\"c:\\\\\\\\windows\\\\path\":1}");
    Expression<Object> actual = compile("`{\"c:\\\\\\\\windows\\\\path\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapedBacktickInJsonString() {
    Expression<Object> expected = JsonLiteral("\"fo`o\"");
    Expression<Object> actual = compile("`\"fo\\`o\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void unEscapedBacktickInJsonString() {
    try {
      compile("`\"fo`o\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 4"));
    }
    try {
      compile("`\"`foo\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"`foo\"`\": unexpected ` at position 2"));
    }
  }

  @Test
  public void comparisonWithJsonLiteralExpression() {
    Expression<Object> expected = Projection(
      Current(),
      Selection(
        Comparison("==",
          Property("bar", Current()),
          JsonLiteral("{\"foo\":\"bar\"}")
        ),
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonBuiltinsAsNames() {
    Expression<Object> expected = Property("true",
      Property("null",
        Property("false", Current())
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
    Expression<Object> expected = Projection(
      Property("bar", Current()),
      FlattenObject(
        Property("foo", Current())
      )
    );
    Expression<Object> actual = compile("foo.*.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleLevelProjectionWithPipe() {
    Expression<Object> expected = Property("baz",
      Projection(
        Property("bar", Current()),
        FlattenObject(
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("foo.*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void multipleLevelsOfProjections() {
    Expression<Object> expected = Property("baz",
      Projection(
        Projection(
          Property("bar", Current()),
          FlattenObject( Current())
        ),
        Slice(null, null, null,
          Property("foo", Current())
        )
      )
    );
    Expression<Object> actual = compile("foo[:].*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation1() {
    Expression<Object> expected = Projection(
      Array(
        Arrays.asList(
          Property("userName", Current()),
          Property("mfaAuthenticated",
            Property("attributes",
              Property("sessionContext", Current())
            )
          )
        ),
        Property("userIdentity", Current())
      ),
      Property("Records", Current())
    );
    Expression<Object> actual = compile("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation2() {
    Expression<Object> expected = Projection(
      Property("keyName",
        Property("requestParameters", Current())
      ),
      Property("Records", Current())
    );
    Expression<Object> actual = compile("Records[*].requestParameters.keyName");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAndFlatten() {
    Expression<Object> expected = Projection(
      Property("instanceId", Current()),
      FlattenArray(
        Projection(
          Property("items", Current()),
          FlattenObject(
            Property("responseElements",
              Index(0,
                Property("Records", Current())
              )
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("Records[0].responseElements.*.items[].instanceId");
    assertThat(actual, is(expected));
  }

  @Test
  public void operationsAfterPipeAfterProjection() {
    Expression<Object> expected = Negate(
      Current(
        Projection(
          Current(),
          Selection(
            String(""),
            Property("Records", Current())
          )
        )
      )
    );
    Expression<Object> actual = compile("Records[?''] | !@");
    assertThat(actual, is(expected));
  }
}
