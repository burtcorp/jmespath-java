package io.burt.jmespath.parser;

import java.util.Arrays;
import java.util.ArrayList;
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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;

public class ParserTest {
  private Adapter<Object> runtime = new JcfRuntime();

  private Expression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private Node<Object> createCurrent() {
    return new CurrentNode<Object>(runtime);
  }

  private Node<Object> createCurrent(Node<Object> source) {
    return new CurrentNode<Object>(runtime, source);
  }

  private Node<Object> createProperty(String name, Node<Object> source) {
    return new PropertyNode<Object>(runtime, name, source);
  }

  private Node<Object> createIndex(int index, Node<Object> source) {
    return new IndexNode<Object>(runtime, index, source);
  }

  private Node<Object> createSlice(Integer start, Integer stop, Integer step, Node<Object> source) {
    return new SliceNode<Object>(runtime, start, stop, step, source);
  }

  private Node<Object> createProjection(Expression<Object> expression, Node<Object> source) {
    return new ProjectionNode<Object>(runtime, expression, source);
  }

  private Node<Object> createFlattenArray(Node<Object> source) {
    return new FlattenArrayNode<Object>(runtime, source);
  }

  private Node<Object> createFlattenObject(Node<Object> source) {
    return new FlattenObjectNode<Object>(runtime, source);
  }

  private Node<Object> createSelection(Expression<Object> test, Node<Object> source) {
    return new SelectionNode<Object>(runtime, test, source);
  }

  private Node<Object> createComparison(String operator, Expression<Object> left, Expression<Object> right) {
    return new ComparisonNode<Object>(runtime, operator, left, right);
  }

  private Node<Object> createOr(Expression<Object> left, Expression<Object> right) {
    return new OrNode<Object>(runtime, left, right);
  }

  private Node<Object> createAnd(Expression<Object> left, Expression<Object> right) {
    return new AndNode<Object>(runtime, left, right);
  }

  private Node<Object> createFunctionCall(String functionName, List<? extends Expression<Object>> args, Node<Object> source) {
    return new FunctionCallNode<Object>(runtime, runtime.getFunction(functionName), args, source);
  }

  private Node<Object> createExpressionReference(Expression<Object> expression) {
    return new ExpressionReferenceNode<Object>(runtime, expression);
  }

  private Node<Object> createString(String str) {
    return new StringNode<Object>(runtime, str);
  }

  private Node<Object> createNegate(Node<Object> source) {
    return new NegateNode<Object>(runtime, source);
  }

  private Node<Object> createObject(List<CreateObjectNode.Entry<Object>> entries, Node<Object> source) {
    return new CreateObjectNode<Object>(runtime, entries, source);
  }

  private Node<Object> createArray(List<? extends Expression<Object>> entries, Node<Object> source) {
    return new CreateArrayNode<Object>(runtime, entries, source);
  }

  private Node<Object> createJsonLiteral(String json) {
    return new JsonLiteralNode<Object>(runtime, json, runtime.parseString(json));
  }

  @Test
  public void identifierExpression() {
    Expression<Object> expected = createProperty("foo", createCurrent());
    Expression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Expression<Object> expected = createProperty("foo-bar", createCurrent());
    Expression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test()
  public void quotedIdentifierExpressionsAreUnescapedLikeJsonStrings() {
    Expression<Object> expected = createProperty("\\foo bar\n", createCurrent());
    Expression<Object> actual = compile("\"\\\\foo\\u0020bar\\n\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Expression<Object> expected = createProperty("bar",
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Expression<Object> expected = createProperty("qux",
      createProperty("baz",
        createProperty("bar",
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Expression<Object> expected = createProperty("bar",
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    Expression<Object> expected = createProperty("qux",
      createProperty("baz",
        createProperty("bar",
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Expression<Object> expected = createProperty("qux",
      createProperty("baz",
        createProperty("bar",
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Expression<Object> expected = createIndex(3,
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Expression<Object> expected = createIndex(3, createCurrent());
    Expression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(3, 4, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(3, null, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(null, 4, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(3, 4, 5,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(3, null, 5,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(null, null, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(null, null, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(0, 1, 2, createCurrent())
    );
    Expression<Object> actual = compile("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Should raise a parse error")
  public void sliceWithZeroStepSize() {
    compile("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createFlattenArray(
          createProperty("foo", createCurrent())
        )
      );
    Expression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createFlattenArray( createCurrent())
    );
    Expression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Expression<Object> expected = createProjection(createCurrent(), createCurrent());
    Expression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createFlattenObject(
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createFlattenObject( createCurrent())
    );
    Expression<Object> actual = compile("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Expression<Object> expected = createCurrent();
    Expression<Object> actual = compile("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    Expression<Object> expected = createCurrent(
      createProperty("bar",
        createCurrent(
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createProperty("bar", createCurrent()),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createComparison("==",
          createProperty("bar", createCurrent()),
          createProperty("baz", createCurrent())
        ),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createProperty("bar", createCurrent()),
        createCurrent()
      )
    );
    Expression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Expression<Object> expected = createFunctionCall("sort",
      Arrays.asList(createCurrent()),
      createCurrent()
    );
    Expression<Object> actual = compile("sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Expression<Object> expected = createFunctionCall("sort",
      Arrays.asList(createProperty("bar", createCurrent())),
      createCurrent()
    );
    Expression<Object> actual = compile("sort(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Expression<Object> expected = createFunctionCall("merge",
      Arrays.asList(
        createProperty("bar", createCurrent()),
        createProperty("baz", createCurrent()),
        createCurrent()
      ),
      createCurrent()
    );
    Expression<Object> actual = compile("merge(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Expression<Object> expected = createFunctionCall("to_string",
      Arrays.asList(createCurrent()),
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    Expression<Object> expected = createFunctionCall("sort",
      Arrays.asList(
        createExpressionReference(
          createProperty("bar",
            createProperty("bar", createCurrent())
          )
        )
      ),
      createCurrent()
    );
    Expression<Object> actual = compile("sort(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
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
    Expression<Object> expected = createString("foo");
    Expression<Object> actual = compile("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createComparison("!=",
          createProperty("bar", createCurrent()),
          createString("baz")
        ),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Expression<Object> expected = createAnd(
      createProperty("foo", createCurrent()),
      createProperty("bar", createCurrent())
    );
    Expression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Expression<Object> expected = createOr(
      createProperty("foo", createCurrent()),
      createProperty("bar", createCurrent())
    );
    Expression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Expression<Object> expected = createIndex(1,
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(1, 2, 1,
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createFlattenArray(
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createProperty("bar", createCurrent()),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createOr(
          createAnd(
            createComparison("!=",
              createProperty("bar", createCurrent()),
              createString("baz")
            ),
            createComparison("==",
              createProperty("qux", createCurrent()),
              createString("fux")
            )
          ),
          createComparison(">",
            createProperty("mux", createCurrent()),
            createString("lux")
          )
        ),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Expression<Object> expected = createFunctionCall("sort",
      Arrays.asList(createCurrent()),
      createProjection(
        createCurrent(),
        createFlattenArray(
          createProperty("bar",
            createProperty("foo", createCurrent())
          )
        )
      )
    );
    Expression<Object> actual = compile("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSlice(2, 3, 1,
        createProperty("qux",
          createProperty("baz",
            createProperty("bar",
              createIndex(3,
                createProperty("foo", createCurrent())
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
  @SuppressWarnings("unchecked")
  public void bareMultiSelectHashExpression() {
    Expression<Object> expected = createObject(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", createString("bar")),
        new CreateObjectNode.Entry<Object>("baz", createCurrent())
      ),
      createCurrent()
    );
    Expression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashExpression() {
    Expression<Object> expected = createObject(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", createString("bar")),
        new CreateObjectNode.Entry<Object>("baz", createCurrent())
      ),
      createProperty("world",
        createProperty("hello", createCurrent())
      )
    );
    Expression<Object> actual = compile("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashWithQuotedKeys() {
    Expression<Object> expected = createObject(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", createString("bar")),
        new CreateObjectNode.Entry<Object>("baz", createCurrent())
      ),
      createCurrent()
    );
    Expression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    Expression<Object> expected = createObject(
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("WashingtonCities",
          createFunctionCall("join",
            Arrays.asList(
              createString(", "),
              createCurrent()
            ),
            createCurrent()
          )
        )
      ),
      createFunctionCall("sort",
        Arrays.asList(createCurrent()),
        createProjection(
          createProperty("name", createCurrent()),
          createSelection(
            createComparison("==",
              createProperty("state", createCurrent()),
              createString("WA")
            ),
            createProperty("locations", createCurrent())
          )
        )
      )
    );
    Expression<Object> actual = compile("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    Expression<Object> expected = createArray(
      Arrays.asList(
        createString("bar"),
        createCurrent()
      ),
      createCurrent()
    );
    Expression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Expression<Object> expected = createArray(
      Arrays.asList(
        createString("bar"),
        createCurrent()
      ),
      createProperty("world",
        createProperty("hello", createCurrent())
      )
    );
    Expression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Expression<Object> expected = createProperty("baz",
      createProperty("bar",
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createAnd(
          createComparison("==",
            createProperty("bar", createCurrent()),
            createString("baz")
          ),
          createOr(
            createComparison("==",
              createProperty("qux", createCurrent()),
              createString("fux")
            ),
            createComparison("==",
              createProperty("mux", createCurrent()),
              createString("lux")
            )
          )
        ),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Expression<Object> expected = createNegate(
      createProperty("foo", createCurrent())
    );
    Expression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createNegate(createProperty("bar", createCurrent())),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    Expression<Object> expected = createJsonLiteral("{}");
    Expression<Object> actual = compile("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    Expression<Object> expected = createJsonLiteral("[]");
    Expression<Object> actual = compile("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    Expression<Object> expected = createJsonLiteral("3.14");
    Expression<Object> actual = compile("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    Expression<Object> expected = createJsonLiteral("\"foo\"");
    Expression<Object> actual = compile("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    Expression<Object> expected = createJsonLiteral("false");
    Expression<Object> actual = compile("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedNewline() {
    Expression<Object> expected = createJsonLiteral("\"hello\nworld\"");
    Expression<Object> actual = compile("`\"hello\\nworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedTab() {
    Expression<Object> expected = createJsonLiteral("\"hello\tworld\"");
    Expression<Object> actual = compile("`\"hello\\tworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedUnicode() {
    Expression<Object> expected = createJsonLiteral("\"hello\\u0020world\"");
    Expression<Object> actual = compile("`\"hello world\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedQuote() {
    Expression<Object> expected = createJsonLiteral("\"hello \\\"world\\\"\"");
    Expression<Object> actual = compile("`\"hello \\\"world\\\"\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedBackslash() {
    Expression<Object> expected = createJsonLiteral("\"c:\\\\\\\\windows\\\\path\"");
    Expression<Object> actual = compile("`\"c:\\\\\\\\windows\\\\path\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedNewlineInKey() {
    Expression<Object> expected = createJsonLiteral("{\"hello\nworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\nworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedTabInKey() {
    Expression<Object> expected = createJsonLiteral("{\"hello\tworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\tworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedUnicodeInKey() {
    Expression<Object> expected = createJsonLiteral("{\"hello\\u0020world\":1}");
    Expression<Object> actual = compile("`{\"hello world\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedQuoteInKey() {
    Expression<Object> expected = createJsonLiteral("{\"hello \\\"world\\\"\":1}");
    Expression<Object> actual = compile("`{\"hello \\\"world\\\"\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedBackslashInKey() {
    Expression<Object> expected = createJsonLiteral("{\"c:\\\\\\\\windows\\\\path\":1}");
    Expression<Object> actual = compile("`{\"c:\\\\\\\\windows\\\\path\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapedBacktickInJsonString() {
    Expression<Object> expected = createJsonLiteral("\"fo`o\"");
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
    Expression<Object> expected = createProjection(
      createCurrent(),
      createSelection(
        createComparison("==",
          createProperty("bar", createCurrent()),
          createJsonLiteral("{\"foo\":\"bar\"}")
        ),
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  public void jsonBuiltinsAsNames() {
    Expression<Object> expected = createProperty("true",
      createProperty("null",
        createProperty("false", createCurrent())
      )
    );
    Expression<Object> actual = compile("false.null.true");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapesInRawStringsArePreserved() {
    Expression<Object> expected = createString("\\u03a6hello\\nworld\\t");
    Expression<Object> actual = compile("'\\u03a6hello\\nworld\\t'");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleQuotesNeedsToBeEscapedInRawStrings() {
    Expression<Object> expected = createString("'");
    Expression<Object> actual = compile("'\\''");
    assertThat(actual, is(expected));
  }

  @Test
  public void backslashesMustBeEscapedInRawStrings() {
    Expression<Object> expected = createString("\\");
    Expression<Object> actual = compile("'\\\\'");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleLevelProjection() {
    Expression<Object> expected = createProjection(
      createProperty("bar", createCurrent()),
      createFlattenObject(
        createProperty("foo", createCurrent())
      )
    );
    Expression<Object> actual = compile("foo.*.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleLevelProjectionWithPipe() {
    Expression<Object> expected = createProperty("baz",
      createProjection(
        createProperty("bar", createCurrent()),
        createFlattenObject(
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("foo.*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void multipleLevelsOfProjections() {
    Expression<Object> expected = createProperty("baz",
      createProjection(
        createProjection(
          createProperty("bar", createCurrent()),
          createFlattenObject( createCurrent())
        ),
        createSlice(null, null, null,
          createProperty("foo", createCurrent())
        )
      )
    );
    Expression<Object> actual = compile("foo[:].*.bar | baz");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation1() {
    Expression<Object> expected = createProjection(
      createArray(
        Arrays.asList(
          createProperty("userName", createCurrent()),
          createProperty("mfaAuthenticated",
            createProperty("attributes",
              createProperty("sessionContext", createCurrent())
            )
          )
        ),
        createProperty("userIdentity", createCurrent())
      ),
      createProperty("Records", createCurrent())
    );
    Expression<Object> actual = compile("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAsFirstOperation2() {
    Expression<Object> expected = createProjection(
      createProperty("keyName",
        createProperty("requestParameters", createCurrent())
      ),
      createProperty("Records", createCurrent())
    );
    Expression<Object> actual = compile("Records[*].requestParameters.keyName");
    assertThat(actual, is(expected));
  }

  @Test
  public void projectionAndFlatten() {
    Expression<Object> expected = createProjection(
      createProperty("instanceId", createCurrent()),
      createFlattenArray(
        createProjection(
          createProperty("items", createCurrent()),
          createFlattenObject(
            createProperty("responseElements",
              createIndex(0,
                createProperty("Records", createCurrent())
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
    Expression<Object> expected = createNegate(
      createCurrent(
        createProjection(
          createCurrent(),
          createSelection(
            createString(""),
            createProperty("Records", createCurrent())
          )
        )
      )
    );
    Expression<Object> actual = compile("Records[?''] | !@");
    assertThat(actual, is(expected));
  }
}
