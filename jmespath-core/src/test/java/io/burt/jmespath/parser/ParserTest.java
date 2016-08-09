package io.burt.jmespath.parser;

import org.junit.Test;
import org.junit.Ignore;

import io.burt.jmespath.JmesPathExpression;
import io.burt.jmespath.Adapter;
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
import io.burt.jmespath.node.JmesPathNode;
import io.burt.jmespath.node.JoinNode;
import io.burt.jmespath.node.JsonLiteralNode;
import io.burt.jmespath.node.ParsedJsonLiteralNode;
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

  private JmesPathExpression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private JsonLiteralNode<Object> createJsonLiteralNode(String json) {
    return new ParsedJsonLiteralNode<Object>(runtime, json, runtime.parseString(json));
  }

  @Test
  public void identifierExpression() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "foo", currentNode);
    JmesPathExpression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "foo-bar", currentNode);
    JmesPathExpression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "bar",
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "qux",
      new PropertyNode<Object>(runtime, "baz",
        new PropertyNode<Object>(runtime, "bar",
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "bar",
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "qux",
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
    JmesPathExpression<Object> actual = compile("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "qux",
      new PropertyNode<Object>(runtime, "baz",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "bar",
            new PropertyNode<Object>(runtime, "foo", currentNode)
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    JmesPathExpression<Object> expected = new IndexNode<Object>(runtime, 3,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    JmesPathExpression<Object> expected = new IndexNode<Object>(runtime, 3, currentNode);
    JmesPathExpression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 3, 4, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 3, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 0, 4, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 3, 4, 5,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 3, 0, 5,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 0, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 0, 0, 1,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 0, 1, 2, currentNode);
    JmesPathExpression<Object> actual = compile("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Should raise a parse error")
  public void sliceWithZeroStepSize() {
    compile("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
    new FlattenArrayNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenArrayNode<Object>(runtime, currentNode)
    );
    JmesPathExpression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime, currentNode);
    JmesPathExpression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenObjectNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenObjectNode<Object>(runtime, currentNode)
    );
    JmesPathExpression<Object> actual = compile("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    JmesPathExpression<Object> expected = currentNode;
    JmesPathExpression<Object> actual = compile("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    JmesPathExpression<Object> expected = new CurrentNode<Object>(runtime,
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
    JmesPathExpression<Object> actual = compile("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "==",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          new PropertyNode<Object>(runtime, "baz", currentNode)
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        currentNode
      )
    );
    JmesPathExpression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void simpleFunctionCallExpression() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime, "foo",
      (JmesPathNode<Object>[]) new JmesPathNode[] {},
      currentNode
    );
    JmesPathExpression<Object> actual = compile("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithArgumentExpression() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime, "foo",
      (JmesPathNode<Object>[]) new JmesPathNode[] {new PropertyNode<Object>(runtime, "bar", currentNode)},
      currentNode
    );
    JmesPathExpression<Object> actual = compile("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithMultipleArgumentsExpression() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime, "foo",
      (JmesPathNode<Object>[]) new JmesPathNode[] {
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new PropertyNode<Object>(runtime, "baz", currentNode),
        currentNode
      },
      currentNode
    );
    JmesPathExpression<Object> actual = compile("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedFunctionCallExpression() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime, "to_string",
      (JmesPathNode<Object>[]) new JmesPathNode[] {currentNode},
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void functionCallWithExpressionReference() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime,
      "foo",
      (JmesPathNode<Object>[]) new JmesPathNode[] {
        new ExpressionReferenceNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "bar",
            new PropertyNode<Object>(runtime, "bar", currentNode)
          )
        )
      },
      currentNode
    );
    JmesPathExpression<Object> actual = compile("foo(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareRawStringExpression() {
    JmesPathExpression<Object> expected = new StringNode<Object>(runtime, "foo");
    JmesPathExpression<Object> actual = compile("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "!=",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          new StringNode<Object>(runtime, "baz")
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    JmesPathExpression<Object> expected = new AndNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode),
      new PropertyNode<Object>(runtime, "bar", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    JmesPathExpression<Object> expected = new OrNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode),
      new PropertyNode<Object>(runtime, "bar", currentNode)
    );
    JmesPathExpression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    JmesPathExpression<Object> expected = new IndexNode<Object>(runtime, 1,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 1, 2, 1,
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new FlattenArrayNode<Object>(runtime,
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar", currentNode),
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "foo", currentNode)
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
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
    JmesPathExpression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainPipeFunctionCallCombination() {
    JmesPathExpression<Object> expected = new FunctionCallNode<Object>(runtime, "sort",
      (JmesPathNode<Object>[]) new JmesPathNode[] {currentNode},
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
    JmesPathExpression<Object> actual = compile("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    JmesPathExpression<Object> expected = new SliceNode<Object>(runtime, 2, 3, 1,
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
    JmesPathExpression<Object> actual = compile("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void bareMultiSelectHashExpression() {
    CreateObjectNode.Entry<Object>[] pieces = (CreateObjectNode.Entry<Object>[]) new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
      new CreateObjectNode.Entry<Object>("baz", currentNode)
    };
    JmesPathExpression<Object> expected = new CreateObjectNode<Object>(runtime, pieces, currentNode);
    JmesPathExpression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashExpression() {
    CreateObjectNode.Entry<Object>[] pieces = (CreateObjectNode.Entry<Object>[]) new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
      new CreateObjectNode.Entry<Object>("baz", currentNode)
    };
    JmesPathExpression<Object> expected = new CreateObjectNode<Object>(runtime, pieces,
      new PropertyNode<Object>(runtime, "world",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "hello", currentNode)
        )
      )
    );
    JmesPathExpression<Object> actual = compile("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectHashWithQuotedKeys() {
    CreateObjectNode.Entry<Object>[] pieces = (CreateObjectNode.Entry<Object>[]) new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry<Object>("foo", new StringNode<Object>(runtime, "bar")),
      new CreateObjectNode.Entry<Object>("baz", currentNode)
    };
    JmesPathExpression<Object> expected = new CreateObjectNode<Object>(runtime, pieces, currentNode);
    JmesPathExpression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void jmesPathSiteExampleExpression() {
    CreateObjectNode.Entry<Object>[] pieces = (CreateObjectNode.Entry<Object>[]) new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry<Object>("WashingtonCities",
        new FunctionCallNode<Object>(runtime, "join",
          (JmesPathNode<Object>[]) new JmesPathNode[] {
            new StringNode<Object>(runtime, ", "),
            currentNode
          },
          currentNode
        )
      )
    };
    JmesPathExpression<Object> expected = new CreateObjectNode<Object>(runtime, pieces,
      new JoinNode<Object>(runtime,
        new FunctionCallNode<Object>(runtime, "sort",
          (JmesPathNode<Object>[]) new JmesPathNode[] {currentNode},
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
    JmesPathExpression<Object> actual = compile("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void bareMultiSelectListExpression() {
    JmesPathExpression<Object> expected = new CreateArrayNode<Object>(runtime,
      (JmesPathNode<Object>[]) new JmesPathNode[] {
        new StringNode<Object>(runtime, "bar"),
        currentNode
      },
      currentNode
    );
    JmesPathExpression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void chainedMultiSelectListExpression() {
    JmesPathExpression<Object> expected = new CreateArrayNode<Object>(runtime,
      (JmesPathNode<Object>[]) new JmesPathNode[] {
        new StringNode<Object>(runtime, "bar"),
        currentNode
      },
      new PropertyNode<Object>(runtime, "world",
        new JoinNode<Object>(runtime,
          new PropertyNode<Object>(runtime, "hello", currentNode)
        )
      )
    );
    JmesPathExpression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "baz",
      new JoinNode<Object>(runtime,
        new PropertyNode<Object>(runtime, "bar",
          new JoinNode<Object>(runtime,
            new PropertyNode<Object>(runtime, "foo", currentNode)
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
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
    JmesPathExpression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    JmesPathExpression<Object> expected = new NegateNode<Object>(runtime,
      new PropertyNode<Object>(runtime, "foo", currentNode)
    );
    JmesPathExpression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new NegateNode<Object>(runtime, new PropertyNode<Object>(runtime, "bar", currentNode)),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("{}");
    JmesPathExpression<Object> actual = compile("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("[]");
    JmesPathExpression<Object> actual = compile("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("3.14");
    JmesPathExpression<Object> actual = compile("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("\"foo\"");
    JmesPathExpression<Object> actual = compile("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("false");
    JmesPathExpression<Object> actual = compile("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void escapedBacktickInJsonString() {
    JmesPathExpression<Object> expected = createJsonLiteralNode("\"fo`o\"");
    JmesPathExpression<Object> actual = compile("`\"fo\\`o\"`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void unEscapedBacktickInJsonString() {
    try {
      compile("`\"fo`o\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 5"));
    }
    try {
      compile("`\"`foo\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 3"));
    }
  }

  @Test
  public void comparisonWithJsonLiteralExpression() {
    JmesPathExpression<Object> expected = new ForkNode<Object>(runtime,
      new SelectionNode<Object>(runtime,
        new ComparisonNode<Object>(runtime, "==",
          new PropertyNode<Object>(runtime, "bar", currentNode),
          createJsonLiteralNode("{\"foo\":\"bar\"}")
        ),
        new PropertyNode<Object>(runtime, "foo", currentNode)
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    JmesPathExpression<Object> expected = new PropertyNode<Object>(runtime, "false", currentNode);
    JmesPathExpression<Object> actual = compile("false");
    assertThat(actual, is(expected));
  }
}
