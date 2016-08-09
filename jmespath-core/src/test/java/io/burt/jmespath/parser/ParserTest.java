package io.burt.jmespath.parser;

import org.junit.Test;
import org.junit.Ignore;

import io.burt.jmespath.JmesPathExpression;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.StandardExpression;
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

  private JmesPathExpression<Object> compile(String str) {
    return runtime.compile(str);
  }

  private JmesPathExpression<Object> build(JmesPathNode node) {
    return new StandardExpression<Object>(runtime, node);
  }

  private JsonLiteralNode createJsonLiteralNode(String json) {
    return new ParsedJsonLiteralNode(json, runtime.parseString(json));
  }

  @Test
  public void identifierExpression() {
    JmesPathExpression<Object> expected = build(new PropertyNode("foo", new CurrentNode()));
    JmesPathExpression<Object> actual = compile("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    JmesPathExpression<Object> expected = build(new PropertyNode("foo-bar", new CurrentNode()));
    JmesPathExpression<Object> actual = compile("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    JmesPathExpression<Object> expected = build(
      new PropertyNode("bar",
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    JmesPathExpression<Object> expected = build(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new PropertyNode("bar",
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    JmesPathExpression<Object> expected = build(
      new PropertyNode("bar",
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    JmesPathExpression<Object> expected = build(
      new PropertyNode("qux",
        new JoinNode(
          new PropertyNode("baz",
            new JoinNode(
              new PropertyNode("bar",
                new JoinNode(
                  new PropertyNode("foo", new CurrentNode())
                )
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
    JmesPathExpression<Object> expected = build(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new JoinNode(
            new PropertyNode("bar",
              new PropertyNode("foo", new CurrentNode())
            )
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    JmesPathExpression<Object> expected = build(
      new IndexNode(3,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    JmesPathExpression<Object> expected = build(new IndexNode(3, new CurrentNode()));
    JmesPathExpression<Object> actual = compile("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(3, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(3, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(0, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(3, 4, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(3, 0, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    JmesPathExpression<Object> expected = build(new SliceNode(0, 1, 2, new CurrentNode()));
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
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new FlattenArrayNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    JmesPathExpression<Object> expected = build(new ForkNode(new FlattenArrayNode(new CurrentNode())));
    JmesPathExpression<Object> actual = compile("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    JmesPathExpression<Object> expected = build(new ForkNode(new PropertyNode("foo", new CurrentNode())));
    JmesPathExpression<Object> actual = compile("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    JmesPathExpression<Object> expected = build(new ForkNode(new CurrentNode()));
    JmesPathExpression<Object> actual = compile("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new FlattenObjectNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    JmesPathExpression<Object> expected = build(new ForkNode(new FlattenObjectNode(new CurrentNode())));
    JmesPathExpression<Object> actual = compile("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    JmesPathExpression<Object> expected = build(new CurrentNode());
    JmesPathExpression<Object> actual = compile("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    JmesPathExpression<Object> expected = build(
      new CurrentNode(
        new JoinNode(
          new PropertyNode("bar",
            new JoinNode(
              new CurrentNode(
                new JoinNode(
                  new PropertyNode("foo",
                    new JoinNode(
                      new CurrentNode()
                    )
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
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new ComparisonNode("==",
            new PropertyNode("bar", new CurrentNode()),
            new PropertyNode("baz", new CurrentNode())
          ),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new CurrentNode()
        )
      )
    );
    JmesPathExpression<Object> actual = compile("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode("foo",
        new JmesPathNode[] {},
        new CurrentNode()
      )
    );
    JmesPathExpression<Object> actual = compile("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode("foo",
        new JmesPathNode[] {new PropertyNode("bar", new CurrentNode())},
        new CurrentNode()
      )
    );
    JmesPathExpression<Object> actual = compile("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode("foo",
        new JmesPathNode[] {
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("baz", new CurrentNode()),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    JmesPathExpression<Object> actual = compile("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode("to_string",
        new JmesPathNode[] {new CurrentNode()},
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode(
        "foo",
        new JmesPathNode[] {
          new ExpressionReferenceNode(
            new PropertyNode("bar",
              new PropertyNode("bar", new CurrentNode())
            )
          )
        },
        new CurrentNode()
      )
    );
    JmesPathExpression<Object> actual = compile("foo(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareRawStringExpression() {
    JmesPathExpression<Object> expected = build(new StringNode("foo"));
    JmesPathExpression<Object> actual = compile("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new ComparisonNode("!=",
            new PropertyNode("bar", new CurrentNode()),
            new StringNode("baz")
          ),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    JmesPathExpression<Object> expected = build(
      new AndNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    JmesPathExpression<Object> expected = build(
      new OrNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    JmesPathExpression<Object> expected = build(
      new IndexNode(1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    JmesPathExpression<Object> expected = build(
      new SliceNode(1, 2, 1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new FlattenArrayNode(
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new OrNode(
            new AndNode(
              new ComparisonNode("!=", new PropertyNode("bar", new CurrentNode()), new StringNode("baz")),
              new ComparisonNode("==", new PropertyNode("qux", new CurrentNode()), new StringNode("fux"))
            ),
            new ComparisonNode(">", new PropertyNode("mux", new CurrentNode()), new StringNode("lux"))
          ),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    JmesPathExpression<Object> expected = build(
      new FunctionCallNode("sort",
        new JmesPathNode[] {new CurrentNode()},
        new JoinNode(
          new ForkNode(
            new FlattenArrayNode(
              new PropertyNode("bar",
                new PropertyNode("foo", new CurrentNode())
              )
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
    JmesPathExpression<Object> expected = build(
      new SliceNode(2, 3, 1,
        new PropertyNode("qux",
          new PropertyNode("baz",
            new JoinNode(
              new PropertyNode("bar",
                new IndexNode(3,
                  new PropertyNode("foo", new CurrentNode())
                )
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
  public void bareMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression<Object> expected = build(new CreateObjectNode(pieces, new CurrentNode()));
    JmesPathExpression<Object> actual = compile("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression<Object> expected = build(
      new CreateObjectNode(pieces,
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashWithQuotedKeys() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression<Object> expected = build(new CreateObjectNode(pieces, new CurrentNode()));
    JmesPathExpression<Object> actual = compile("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("WashingtonCities",
        new FunctionCallNode("join",
          new JmesPathNode[] {
            new StringNode(", "),
            new CurrentNode()
          },
          new CurrentNode()
        )
      )
    };
    JmesPathExpression<Object> expected = build(
      new CreateObjectNode(pieces,
        new JoinNode(
          new FunctionCallNode("sort",
            new JmesPathNode[] {new CurrentNode()},
            new JoinNode(
              new PropertyNode("name",
                new ForkNode(
                  new SelectionNode(
                    new ComparisonNode("==",
                      new PropertyNode("state", new CurrentNode()),
                      new StringNode("WA")
                    ),
                    new PropertyNode("locations", new CurrentNode())
                  )
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
  public void bareMultiSelectListExpression() {
    JmesPathExpression<Object> expected = build(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    JmesPathExpression<Object> actual = compile("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    JmesPathExpression<Object> expected = build(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          new CurrentNode()
        },
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    JmesPathExpression<Object> expected = build(
      new PropertyNode("baz",
        new JoinNode(
          new PropertyNode("bar",
            new JoinNode(
              new PropertyNode("foo", new CurrentNode())
            )
          )
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new AndNode(
            new ComparisonNode("==",
              new PropertyNode("bar", new CurrentNode()),
              new StringNode("baz")
            ),
            new OrNode(
              new ComparisonNode("==",
                new PropertyNode("qux", new CurrentNode()),
                new StringNode("fux")
              ),
              new ComparisonNode("==",
                new PropertyNode("mux", new CurrentNode()),
                new StringNode("lux")
              )
            )
          ),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    JmesPathExpression<Object> expected = build(
      new NegateNode(
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression<Object> actual = compile("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new NegateNode(new PropertyNode("bar", new CurrentNode())),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("{}"));
    JmesPathExpression<Object> actual = compile("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("[]"));
    JmesPathExpression<Object> actual = compile("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("3.14"));
    JmesPathExpression<Object> actual = compile("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("\"foo\""));
    JmesPathExpression<Object> actual = compile("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("false"));
    JmesPathExpression<Object> actual = compile("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void escapedBacktickInJsonString() {
    JmesPathExpression<Object> expected = build(createJsonLiteralNode("\"fo`o\""));
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
    JmesPathExpression<Object> expected = build(
      new ForkNode(
        new SelectionNode(
          new ComparisonNode("==",
            new PropertyNode("bar", new CurrentNode()),
            createJsonLiteralNode("{\"foo\":\"bar\"}")
          ),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression<Object> actual = compile("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    JmesPathExpression<Object> expected = build(new PropertyNode("false", new CurrentNode()));
    JmesPathExpression<Object> actual = compile("false");
    assertThat(actual, is(expected));
  }
}
