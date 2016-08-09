package io.burt.jmespath.parser;

import org.junit.Test;
import org.junit.Ignore;

import io.burt.jmespath.JmesPathExpression;
import io.burt.jmespath.JmesPathRuntime;
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
  private JmesPathRuntime<Object> runtime = new JcfRuntime();

  private JmesPathExpression parse(String str) {
    return JmesPathExpression.fromString(runtime, str);
  }

  private JsonLiteralNode createJsonLiteralNode(String json) {
    return new ParsedJsonLiteralNode(json, runtime.parseString(json));
  }

  @Test
  public void identifierExpression() {
    JmesPathExpression expected = new JmesPathExpression(new PropertyNode("foo", new CurrentNode()));
    JmesPathExpression actual = parse("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    JmesPathExpression expected = new JmesPathExpression(new PropertyNode("foo-bar", new CurrentNode()));
    JmesPathExpression actual = parse("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new PropertyNode("bar",
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new PropertyNode("bar",
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression actual = parse("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    JmesPathExpression expected = new JmesPathExpression(
      new PropertyNode("bar",
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new IndexNode(3,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    JmesPathExpression expected = new JmesPathExpression(new IndexNode(3, new CurrentNode()));
    JmesPathExpression actual = parse("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(3, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(3, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(0, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(3, 4, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(3, 0, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    JmesPathExpression expected = new JmesPathExpression(new SliceNode(0, 1, 2, new CurrentNode()));
    JmesPathExpression actual = parse("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Should raise a parse error")
  public void sliceWithZeroStepSize() {
    parse("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new FlattenArrayNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    JmesPathExpression expected = new JmesPathExpression(new ForkNode(new FlattenArrayNode(new CurrentNode())));
    JmesPathExpression actual = parse("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    JmesPathExpression expected = new JmesPathExpression(new ForkNode(new PropertyNode("foo", new CurrentNode())));
    JmesPathExpression actual = parse("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    JmesPathExpression expected = new JmesPathExpression(new ForkNode(new CurrentNode()));
    JmesPathExpression actual = parse("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new FlattenObjectNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    JmesPathExpression expected = new JmesPathExpression(new ForkNode(new FlattenObjectNode(new CurrentNode())));
    JmesPathExpression actual = parse("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    JmesPathExpression expected = new JmesPathExpression(new CurrentNode());
    JmesPathExpression actual = parse("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new CurrentNode()
        )
      )
    );
    JmesPathExpression actual = parse("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new FunctionCallNode("foo",
        new JmesPathNode[] {},
        new CurrentNode()
      )
    );
    JmesPathExpression actual = parse("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new FunctionCallNode("foo",
        new JmesPathNode[] {new PropertyNode("bar", new CurrentNode())},
        new CurrentNode()
      )
    );
    JmesPathExpression actual = parse("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new FunctionCallNode("foo",
        new JmesPathNode[] {
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("baz", new CurrentNode()),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    JmesPathExpression actual = parse("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new FunctionCallNode("to_string",
        new JmesPathNode[] {new CurrentNode()},
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareRawStringExpression() {
    JmesPathExpression expected = new JmesPathExpression(new StringNode("foo"));
    JmesPathExpression actual = parse("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new AndNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new OrNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    JmesPathExpression expected = new JmesPathExpression(
      new IndexNode(1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    JmesPathExpression expected = new JmesPathExpression(
      new SliceNode(1, 2, 1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new FlattenArrayNode(
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression actual = parse("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression actual = parse("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression expected = new JmesPathExpression(new CreateObjectNode(pieces, new CurrentNode()));
    JmesPathExpression actual = parse("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression expected = new JmesPathExpression(
      new CreateObjectNode(pieces,
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", new CurrentNode())
          )
        )
      )
    );
    JmesPathExpression actual = parse("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashWithQuotedKeys() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    JmesPathExpression expected = new JmesPathExpression(new CreateObjectNode(pieces, new CurrentNode()));
    JmesPathExpression actual = parse("{\"foo\": 'bar', \"baz\": @}");
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
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    JmesPathExpression actual = parse("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new NegateNode(
        new PropertyNode("foo", new CurrentNode())
      )
    );
    JmesPathExpression actual = parse("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    JmesPathExpression expected = new JmesPathExpression(
      new ForkNode(
        new SelectionNode(
          new NegateNode(new PropertyNode("bar", new CurrentNode())),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    JmesPathExpression actual = parse("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("{}"));
    JmesPathExpression actual = parse("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("[]"));
    JmesPathExpression actual = parse("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("3.14"));
    JmesPathExpression actual = parse("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("\"foo\""));
    JmesPathExpression actual = parse("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("false"));
    JmesPathExpression actual = parse("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void escapedBacktickInJsonString() {
    JmesPathExpression expected = new JmesPathExpression(createJsonLiteralNode("\"fo`o\""));
    JmesPathExpression actual = parse("`\"fo\\`o\"`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void unEscapedBacktickInJsonString() {
    try {
      parse("`\"fo`o\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 5"));
    }
    try {
      parse("`\"`foo\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 3"));
    }
  }

  @Test
  public void comparisonWithJsonLiteralExpression() {
    JmesPathExpression expected = new JmesPathExpression(
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
    JmesPathExpression actual = parse("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    JmesPathExpression expected = new JmesPathExpression(new PropertyNode("false", new CurrentNode()));
    JmesPathExpression actual = parse("false");
    assertThat(actual, is(expected));
  }
}
