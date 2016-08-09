package io.burt.jmespath.parser;

import org.junit.Test;
import org.junit.Ignore;

import io.burt.jmespath.Query;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.jcf.JcfAdapter;
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
  private Adapter<Object> adapter = new JcfAdapter();

  private Query parse(String str) {
    return Query.fromString(adapter, str);
  }

  private JsonLiteralNode createJsonLiteralNode(String json) {
    return new ParsedJsonLiteralNode(json, adapter.parseString(json));
  }

  @Test
  public void identifierExpression() {
    Query expected = new Query(new PropertyNode("foo", new CurrentNode()));
    Query actual = parse("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Query expected = new Query(new PropertyNode("foo-bar", new CurrentNode()));
    Query actual = parse("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Query expected = new Query(
      new PropertyNode("bar",
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Query expected = new Query(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new PropertyNode("bar",
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    Query actual = parse("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Query expected = new Query(
      new PropertyNode("bar",
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpressionWithoutProjection() {
    Query expected = new Query(
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
    Query actual = parse("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Query expected = new Query(
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
    Query actual = parse("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Query expected = new Query(
      new IndexNode(3,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Query expected = new Query(new IndexNode(3, new CurrentNode()));
    Query actual = parse("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Query expected = new Query(
      new SliceNode(3, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Query expected = new Query(
      new SliceNode(3, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Query expected = new Query(
      new SliceNode(0, 4, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Query expected = new Query(
      new SliceNode(3, 4, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Query expected = new Query(
      new SliceNode(3, 0, 5,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Query expected = new Query(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Query expected = new Query(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Query expected = new Query(new SliceNode(0, 1, 2, new CurrentNode()));
    Query actual = parse("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Should raise a parse error")
  public void sliceWithZeroStepSize() {
    parse("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    Query expected = new Query(
      new ForkNode(
        new FlattenArrayNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Query expected = new Query(new ForkNode(new FlattenArrayNode(new CurrentNode())));
    Query actual = parse("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Query expected = new Query(new ForkNode(new PropertyNode("foo", new CurrentNode())));
    Query actual = parse("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Query expected = new Query(new ForkNode(new CurrentNode()));
    Query actual = parse("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Query expected = new Query(
      new ForkNode(
        new FlattenObjectNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Query expected = new Query(new ForkNode(new FlattenObjectNode(new CurrentNode())));
    Query actual = parse("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Query expected = new Query(new CurrentNode());
    Query actual = parse("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeInPipes() {
    Query expected = new Query(
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
    Query actual = parse("@ | foo | @ | bar | @");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Query expected = new Query(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Query expected = new Query(
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
    Query actual = parse("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Query expected = new Query(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new CurrentNode()
        )
      )
    );
    Query actual = parse("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {},
        new CurrentNode()
      )
    );
    Query actual = parse("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {new PropertyNode("bar", new CurrentNode())},
        new CurrentNode()
      )
    );
    Query actual = parse("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {
          new PropertyNode("bar", new CurrentNode()),
          new PropertyNode("baz", new CurrentNode()),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    Query actual = parse("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Query expected = new Query(
      new FunctionCallNode("to_string",
        new JmesPathNode[] {new CurrentNode()},
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    Query expected = new Query(
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
    Query actual = parse("foo(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareRawStringExpression() {
    Query expected = new Query(new StringNode("foo"));
    Query actual = parse("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    Query expected = new Query(
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
    Query actual = parse("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Query expected = new Query(
      new AndNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    Query actual = parse("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Query expected = new Query(
      new OrNode(
        new PropertyNode("foo", new CurrentNode()),
        new PropertyNode("bar", new CurrentNode())
      )
    );
    Query actual = parse("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Query expected = new Query(
      new ForkNode(
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Query expected = new Query(
      new IndexNode(1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Query expected = new Query(
      new SliceNode(1, 2, 1,
        new JoinNode(
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Query expected = new Query(
      new ForkNode(
        new FlattenArrayNode(
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    Query actual = parse("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Query expected = new Query(
      new ForkNode(
        new SelectionNode(
          new PropertyNode("bar", new CurrentNode()),
          new JoinNode(
            new PropertyNode("foo", new CurrentNode())
          )
        )
      )
    );
    Query actual = parse("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Query expected = new Query(
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
    Query actual = parse("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Query expected = new Query(
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
    Query actual = parse("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Query expected = new Query(
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
    Query actual = parse("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    Query expected = new Query(new CreateObjectNode(pieces, new CurrentNode()));
    Query actual = parse("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    Query expected = new Query(
      new CreateObjectNode(pieces,
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", new CurrentNode())
          )
        )
      )
    );
    Query actual = parse("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashWithQuotedKeys() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", new CurrentNode())
    };
    Query expected = new Query(new CreateObjectNode(pieces, new CurrentNode()));
    Query actual = parse("{\"foo\": 'bar', \"baz\": @}");
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
    Query expected = new Query(
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
    Query actual = parse("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    Query expected = new Query(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          new CurrentNode()
        },
        new CurrentNode()
      )
    );
    Query actual = parse("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Query expected = new Query(
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
    Query actual = parse("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Query expected = new Query(
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
    Query actual = parse("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Query expected = new Query(
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
    Query actual = parse("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Query expected = new Query(
      new NegateNode(
        new PropertyNode("foo", new CurrentNode())
      )
    );
    Query actual = parse("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Query expected = new Query(
      new ForkNode(
        new SelectionNode(
          new NegateNode(new PropertyNode("bar", new CurrentNode())),
          new PropertyNode("foo", new CurrentNode())
        )
      )
    );
    Query actual = parse("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    Query expected = new Query(createJsonLiteralNode("{}"));
    Query actual = parse("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    Query expected = new Query(createJsonLiteralNode("[]"));
    Query actual = parse("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    Query expected = new Query(createJsonLiteralNode("3.14"));
    Query actual = parse("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    Query expected = new Query(createJsonLiteralNode("\"foo\""));
    Query actual = parse("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    Query expected = new Query(createJsonLiteralNode("false"));
    Query actual = parse("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void escapedBacktickInJsonString() {
    Query expected = new Query(createJsonLiteralNode("\"fo`o\""));
    Query actual = parse("`\"fo\\`o\"`");
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
    Query expected = new Query(
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
    Query actual = parse("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    Query expected = new Query(new PropertyNode("false", new CurrentNode()));
    Query actual = parse("false");
    assertThat(actual, is(expected));
  }
}
