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
import io.burt.jmespath.node.ForkNode;
import io.burt.jmespath.node.FunctionCallNode;
import io.burt.jmespath.node.IndexNode;
import io.burt.jmespath.node.Node;
import io.burt.jmespath.node.JoinNode;
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
  private CurrentNode<Object> currentNode = new CurrentNode<Object>(runtime);

  private Expression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private JsonLiteralNode<Object> createJsonLiteralNode(String json) {
    return new JsonLiteralNode<Object>(runtime, json, runtime.parseString(json));
  }

  @SafeVarargs
  private final List<Expression<Object>> asExpressionList(Expression<Object>... expressions) {
    return Arrays.asList(expressions);
  }

  @Test
  public void identifierExpression() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "foo", currentNode);
    Expression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "foo-bar", currentNode);
    Expression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test()
  public void quotedIdentifierExpressionsAreUnescapedLikeJsonStrings() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "\\foo bar\n", currentNode);
    Expression<Object> actual = compile("\"\\\\foo\\u0020bar\\n\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "bar",
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "qux",
      new PropertyNode<Object>(runtime, "baz",
        new PropertyNode<Object>(runtime, "bar",
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    Expression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "bar",
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "qux",
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "baz",
          new JoinNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "bar",
              new JoinNode<Object>(runtime,
                new PropertyNode<Object>(runtime, "foo", currentNode)
              )
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "qux",
      new PropertyNode<Object>(runtime, "baz",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "bar",
            new PropertyNode<Object>(runtime, "foo", currentNode)
          )
        )
      )
    );
    Expression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Expression<Object> expected = new IndexNode<Object>(runtime, 3,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Expression<Object> expected = new IndexNode<Object>(runtime, 3, currentNode);
    Expression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 3, 4, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 3, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 0, 4, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 3, 4, 5,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 3, 0, 5,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 0, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 0, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 0, 1, 2, currentNode);
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
    Expression<Object> expected = new ForkNode<Object>(runtime,
    new FlattenArrayNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenArrayNode<Object>(runtime, currentNode)
    );
    Expression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime, currentNode);
    Expression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenObjectNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenObjectNode<Object>(runtime, currentNode)
    );
    Expression<Object> actual = compile("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Expression<Object> expected = currentNode;
    Expression<Object> actual = compile("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    Expression<Object> expected = new CurrentNode<Object>(runtime,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar",
          new JoinNode<Object>(runtime,
            new CurrentNode<Object>(runtime,
              new JoinNode<Object>(runtime,
                new PropertyNode<Object>(runtime, "foo",
                  new JoinNode<Object>(runtime,
                    currentNode
                  )
                )
              )
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "==",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          new PropertyNode<Object>(runtime, "baz", currentNode)
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        currentNode
      )
    );
    Expression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void simpleFunctionCallExpression() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("sort"),
      Arrays.asList(currentNode),
      currentNode
    );
    Expression<Object> actual = compile("sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithArgumentExpression() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("sort"),
      Arrays.asList(
        new PropertyNode<Object>(runtime, "bar", currentNode)
      ),
      currentNode
    );
    Expression<Object> actual = compile("sort(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithMultipleArgumentsExpression() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("merge"),
      Arrays.asList(
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new PropertyNode<Object>(runtime, "baz", currentNode),
        currentNode
      ),
      currentNode
    );
    Expression<Object> actual = compile("merge(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedFunctionCallExpression() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("to_string"),
      Arrays.asList(currentNode),
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithExpressionReference() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("sort"),
      Arrays.asList(
        new ExpressionReferenceNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "bar",
            new PropertyNode<Object>(runtime, "bar", currentNode)
          )
        )
      ),
      currentNode
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
    Expression<Object> expected = new StringNode<Object>(runtime, "foo");
    Expression<Object> actual = compile("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "!=",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          new StringNode<Object>(runtime, "baz")
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Expression<Object> expected = new AndNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode),
      new PropertyNode<Object>(runtime, "bar", currentNode)
    );
    Expression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Expression<Object> expected = new OrNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode),
      new PropertyNode<Object>(runtime, "bar", currentNode)
    );
    Expression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Expression<Object> expected = new IndexNode<Object>(runtime, 1,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 1, 2, 1,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenArrayNode<Object>(runtime,
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    Expression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    Expression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new OrNode<Object>(runtime,
          new AndNode<Object>(runtime,
            new ComparisonNode<Object>(runtime, "!=",
              new PropertyNode<Object>(runtime, "bar", currentNode),
              new StringNode<Object>(runtime, "baz")
            ),
            new ComparisonNode<Object>(runtime, "==",
              new PropertyNode<Object>(runtime, "qux", currentNode),
              new StringNode<Object>(runtime, "fux")
            )
          ),
          new ComparisonNode<Object>(runtime, ">",
            new PropertyNode<Object>(runtime, "mux", currentNode),
            new StringNode<Object>(runtime, "lux")
          )
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainPipeFunctionCallCombination() {
    Expression<Object> expected = new FunctionCallNode<Object>(runtime,
      runtime.getFunction("sort"),
      Arrays.asList(currentNode),
      new JoinNode<Object>(runtime,
        new ForkNode<Object>(runtime,
          new FlattenArrayNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "bar",
              new PropertyNode<Object>(runtime, "foo", currentNode)
            )
          )
        )
      )
    );
    Expression<Object> actual = compile("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Expression<Object> expected = new SliceNode<Object>(runtime, 2, 3, 1,
      new PropertyNode<Object>(runtime, "qux",
        new PropertyNode<Object>(runtime, "baz",
          new JoinNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "bar",
              new IndexNode<Object>(runtime, 3,
                new PropertyNode<Object>(runtime, "foo", currentNode)
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
    Expression<Object> expected = new CreateObjectNode<Object>(runtime,
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
        new CreateObjectNode.Entry<Object>("baz", currentNode)
      ),
      currentNode
    );
    Expression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashExpression() {
    Expression<Object> expected = new CreateObjectNode<Object>(runtime,
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
        new CreateObjectNode.Entry<Object>("baz", currentNode)
      ),
      new PropertyNode<Object>(runtime, "world",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "hello", currentNode)
        )
      )
    );
    Expression<Object> actual = compile("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashWithQuotedKeys() {
    Expression<Object> expected = new CreateObjectNode<Object>(runtime,
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
        new CreateObjectNode.Entry<Object>("baz", currentNode)
      ),
      currentNode
    );
    Expression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void jmesPathSiteExampleExpression() {
    Expression<Object> expected = new CreateObjectNode<Object>(runtime,
      Arrays.asList(
        new CreateObjectNode.Entry<Object>("WashingtonCities",
          new FunctionCallNode<Object>(runtime,
            runtime.getFunction("join"),
            Arrays.asList(
              new StringNode<Object>(runtime, ", "),
              currentNode
            ),
            currentNode
          )
        )
      ),
      new JoinNode<Object>(runtime,
        new FunctionCallNode<Object>(runtime,
          runtime.getFunction("sort"),
          Arrays.asList(currentNode),
          new JoinNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "name",
              new ForkNode<Object>(runtime,
                new SelectionNode<Object>(runtime,
                  new ComparisonNode<Object>(runtime, "==",
                    new PropertyNode<Object>(runtime, "state", currentNode),
                    new StringNode<Object>(runtime, "WA")
                  ),
                  new PropertyNode<Object>(runtime, "locations", currentNode)
                )
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
  @SuppressWarnings("unchecked")
  public void bareMultiSelectListExpression() {
    Expression<Object> expected = new CreateArrayNode<Object>(runtime,
      asExpressionList(
        new StringNode<Object>(runtime, "bar"),
        currentNode
      ),
      currentNode
    );
    Expression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectListExpression() {
    Expression<Object> expected = new CreateArrayNode<Object>(runtime,
      asExpressionList(
        new StringNode<Object>(runtime, "bar"),
        currentNode
      ),
      new PropertyNode<Object>(runtime, "world",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "hello", currentNode)
        )
      )
    );
    Expression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "baz",
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar",
          new JoinNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "foo", currentNode)
          )
        )
      )
    );
    Expression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new AndNode<Object>(runtime,
          new ComparisonNode<Object>(runtime, "==",
            new PropertyNode<Object>(runtime, "bar", currentNode),
            new StringNode<Object>(runtime, "baz")
          ),
          new OrNode<Object>(runtime,
            new ComparisonNode<Object>(runtime, "==",
              new PropertyNode<Object>(runtime, "qux", currentNode),
              new StringNode<Object>(runtime, "fux")
            ),
            new ComparisonNode<Object>(runtime, "==",
              new PropertyNode<Object>(runtime, "mux", currentNode),
              new StringNode<Object>(runtime, "lux")
            )
          )
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Expression<Object> expected = new NegateNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    Expression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new NegateNode<Object>(runtime, new PropertyNode<Object>(runtime, "bar", currentNode)),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    Expression<Object> expected = createJsonLiteralNode("{}");
    Expression<Object> actual = compile("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    Expression<Object> expected = createJsonLiteralNode("[]");
    Expression<Object> actual = compile("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    Expression<Object> expected = createJsonLiteralNode("3.14");
    Expression<Object> actual = compile("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    Expression<Object> expected = createJsonLiteralNode("\"foo\"");
    Expression<Object> actual = compile("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    Expression<Object> expected = createJsonLiteralNode("false");
    Expression<Object> actual = compile("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedNewline() {
    Expression<Object> expected = createJsonLiteralNode("\"hello\nworld\"");
    Expression<Object> actual = compile("`\"hello\\nworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedTab() {
    Expression<Object> expected = createJsonLiteralNode("\"hello\tworld\"");
    Expression<Object> actual = compile("`\"hello\\tworld\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedUnicode() {
    Expression<Object> expected = createJsonLiteralNode("\"hello\\u0020world\"");
    Expression<Object> actual = compile("`\"hello world\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedQuote() {
    Expression<Object> expected = createJsonLiteralNode("\"hello \\\"world\\\"\"");
    Expression<Object> actual = compile("`\"hello \\\"world\\\"\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralStringWithEscapedBackslash() {
    Expression<Object> expected = createJsonLiteralNode("\"c:\\\\\\\\windows\\\\path\"");
    Expression<Object> actual = compile("`\"c:\\\\\\\\windows\\\\path\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedNewlineInKey() {
    Expression<Object> expected = createJsonLiteralNode("{\"hello\nworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\nworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedTabInKey() {
    Expression<Object> expected = createJsonLiteralNode("{\"hello\tworld\":1}");
    Expression<Object> actual = compile("`{\"hello\\tworld\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedUnicodeInKey() {
    Expression<Object> expected = createJsonLiteralNode("{\"hello\\u0020world\":1}");
    Expression<Object> actual = compile("`{\"hello world\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedQuoteInKey() {
    Expression<Object> expected = createJsonLiteralNode("{\"hello \\\"world\\\"\":1}");
    Expression<Object> actual = compile("`{\"hello \\\"world\\\"\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralObjectWithEscapedBackslashInKey() {
    Expression<Object> expected = createJsonLiteralNode("{\"c:\\\\\\\\windows\\\\path\":1}");
    Expression<Object> actual = compile("`{\"c:\\\\\\\\windows\\\\path\":1}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapedBacktickInJsonString() {
    Expression<Object> expected = createJsonLiteralNode("\"fo`o\"");
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
    Expression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "==",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          createJsonLiteralNode("{\"foo\":\"bar\"}")
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    Expression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    Expression<Object> expected = new PropertyNode<Object>(runtime, "false", currentNode);
    Expression<Object> actual = compile("false");
    assertThat(actual, is(expected));
  }

  @Test
  public void escapesInRawStringsArePreserved() {
    Expression<Object> expected = new StringNode<Object>(runtime, "\\u03a6hello\\nworld\\t");
    Expression<Object> actual = compile("'\\u03a6hello\\nworld\\t'");
    assertThat(actual, is(expected));
  }

  @Test
  public void singleQuotesNeedsToBeEscapedInRawStrings() {
    Expression<Object> expected = new StringNode<Object>(runtime, "'");
    Expression<Object> actual = compile("'\\''");
    assertThat(actual, is(expected));
  }

  @Test
  public void backslashesMustBeEscapedInRawStrings() {
    Expression<Object> expected = new StringNode<Object>(runtime, "\\");
    Expression<Object> actual = compile("'\\\\'");
    assertThat(actual, is(expected));
  }
}
