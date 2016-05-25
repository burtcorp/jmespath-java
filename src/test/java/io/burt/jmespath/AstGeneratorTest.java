package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import io.burt.jmespath.Query;
import io.burt.jmespath.ast.AndNode;
import io.burt.jmespath.ast.ComparisonNode;
import io.burt.jmespath.ast.CreateArrayNode;
import io.burt.jmespath.ast.CreateObjectNode;
import io.burt.jmespath.ast.CurrentNode;
import io.burt.jmespath.ast.ExpressionReferenceNode;
import io.burt.jmespath.ast.FlattenListNode;
import io.burt.jmespath.ast.FlattenObjectNode;
import io.burt.jmespath.ast.ForkNode;
import io.burt.jmespath.ast.FunctionCallNode;
import io.burt.jmespath.ast.IndexNode;
import io.burt.jmespath.ast.JmesPathNode;
import io.burt.jmespath.ast.JoinNode;
import io.burt.jmespath.ast.JsonLiteralNode;
import io.burt.jmespath.ast.NegateNode;
import io.burt.jmespath.ast.OrNode;
import io.burt.jmespath.ast.PropertyNode;
import io.burt.jmespath.ast.SelectionNode;
import io.burt.jmespath.ast.SliceNode;
import io.burt.jmespath.ast.StringNode;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasEntry;

public class AstGeneratorTest {
  @Test
  public void identifierExpression() {
    Query expected = new Query(new PropertyNode("foo", CurrentNode.instance));
    Query actual = AstGenerator.fromString("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void quotedIdentifierExpression() {
    Query expected = new Query(new PropertyNode("foo-bar", CurrentNode.instance));
    Query actual = AstGenerator.fromString("\"foo-bar\"");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Query expected = new Query(
      new PropertyNode("bar",
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Query expected = new Query(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new PropertyNode("bar",
            new PropertyNode("foo", CurrentNode.instance)
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpressionWithoutProjection() {
    Query expected = new Query(
      new PropertyNode("bar",
        new JoinNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | bar");
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
                  new PropertyNode("foo", CurrentNode.instance)
                )
              )
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipesAndChains() {
    Query expected = new Query(
      new PropertyNode("qux",
        new PropertyNode("baz",
          new JoinNode(
            new PropertyNode("bar",
              new PropertyNode("foo", CurrentNode.instance)
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo.bar | baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Query expected = new Query(
      new IndexNode(3,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Query expected = new Query(new IndexNode(3, CurrentNode.instance));
    Query actual = AstGenerator.fromString("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Query expected = new Query(
      new SliceNode(3, 4, 1,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Query expected = new Query(
      new SliceNode(3, 0, 1,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Query expected = new Query(
      new SliceNode(0, 4, 1,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Query expected = new Query(
      new SliceNode(3, 4, 5,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Query expected = new Query(
      new SliceNode(3, 0, 5,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Query expected = new Query(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Query expected = new Query(
      new SliceNode(0, 0, 1,
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Query expected = new Query(new SliceNode(0, 1, 2, CurrentNode.instance));
    Query actual = AstGenerator.fromString("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Should raise a parse error")
  public void sliceWithZeroStepSize() {
    AstGenerator.fromString("[0:1:0]");
  }

  @Test
  public void flattenExpression() {
    Query expected = new Query(
      new ForkNode(
        new FlattenListNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Query expected = new Query(new ForkNode(new FlattenListNode(CurrentNode.instance)));
    Query actual = AstGenerator.fromString("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Query expected = new Query(new ForkNode(new PropertyNode("foo", CurrentNode.instance)));
    Query actual = AstGenerator.fromString("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Query expected = new Query(new ForkNode(CurrentNode.instance));
    Query actual = AstGenerator.fromString("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Query expected = new Query(
      new ForkNode(
        new FlattenObjectNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Query expected = new Query(new ForkNode(new FlattenObjectNode(CurrentNode.instance)));
    Query actual = AstGenerator.fromString("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Query expected = new Query(CurrentNode.instance);
    Query actual = AstGenerator.fromString("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Query expected = new Query(
      new SelectionNode(
        new PropertyNode("bar", CurrentNode.instance),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Query expected = new Query(
      new SelectionNode(
        new ComparisonNode("==",
          new PropertyNode("bar", CurrentNode.instance),
          new PropertyNode("baz", CurrentNode.instance)
        ),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Query expected = new Query(
      new SelectionNode(
        new PropertyNode("bar", CurrentNode.instance),
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {},
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {new PropertyNode("bar", CurrentNode.instance)},
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Query expected = new Query(
      new FunctionCallNode("foo",
        new JmesPathNode[] {
          new PropertyNode("bar", CurrentNode.instance),
          new PropertyNode("baz", CurrentNode.instance),
          CurrentNode.instance
        },
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Query expected = new Query(
      new FunctionCallNode("to_string",
        new JmesPathNode[] {CurrentNode.instance},
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo.to_string(@)");
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
              new PropertyNode("bar", CurrentNode.instance)
            )
          )
        },
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("foo(&bar.bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareRawStringExpression() {
    Query expected = new Query(new StringNode("foo"));
    Query actual = AstGenerator.fromString("'foo'");
    assertThat(actual, is(expected));
  }

  @Test
  public void rawStringComparisonExpression() {
    Query expected = new Query(
      new SelectionNode(
        new ComparisonNode("!=",
          new PropertyNode("bar", CurrentNode.instance),
          new StringNode("baz")
        ),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Query expected = new Query(
      new AndNode(
        new PropertyNode("foo", CurrentNode.instance),
        new PropertyNode("bar", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Query expected = new Query(
      new OrNode(
        new PropertyNode("foo", CurrentNode.instance),
        new PropertyNode("bar", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void wildcardAfterPipe() {
    Query expected = new Query(
      new ForkNode(
        new JoinNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexAfterPipe() {
    Query expected = new Query(
      new IndexNode(1,
        new JoinNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceAfterPipe() {
    Query expected = new Query(
      new SliceNode(1, 2, 1,
        new JoinNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenAfterPipe() {
    Query expected = new Query(
      new ForkNode(
        new FlattenListNode(
          new JoinNode(
            new PropertyNode("foo", CurrentNode.instance)
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionAfterPipe() {
    Query expected = new Query(
      new SelectionNode(
        new PropertyNode("bar", CurrentNode.instance),
        new JoinNode(
          new PropertyNode("foo", CurrentNode.instance)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Query expected = new Query(
      new SelectionNode(
        new OrNode(
          new AndNode(
            new ComparisonNode("!=", new PropertyNode("bar", CurrentNode.instance), new StringNode("baz")),
            new ComparisonNode("==", new PropertyNode("qux", CurrentNode.instance), new StringNode("fux"))
          ),
          new ComparisonNode(">", new PropertyNode("mux", CurrentNode.instance), new StringNode("lux"))
        ),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Query expected = new Query(
      new FunctionCallNode("sort",
        new JmesPathNode[] {CurrentNode.instance},
        new JoinNode(
          new ForkNode(
            new FlattenListNode(
              new PropertyNode("bar",
                new PropertyNode("foo", CurrentNode.instance)
              )
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo.bar[] | sort(@)");
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
                  new PropertyNode("foo", CurrentNode.instance)
                )
              )
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", CurrentNode.instance)
    };
    Query expected = new Query(new CreateObjectNode(pieces, CurrentNode.instance));
    Query actual = AstGenerator.fromString("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", CurrentNode.instance)
    };
    Query expected = new Query(
      new CreateObjectNode(pieces,
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", CurrentNode.instance)
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashWithQuotedKeys() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("foo", new StringNode("bar")),
      new CreateObjectNode.Entry("baz", CurrentNode.instance)
    };
    Query expected = new Query(new CreateObjectNode(pieces, CurrentNode.instance));
    Query actual = AstGenerator.fromString("{\"foo\": 'bar', \"baz\": @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    CreateObjectNode.Entry[] pieces = new CreateObjectNode.Entry[] {
      new CreateObjectNode.Entry("WashingtonCities",
        new FunctionCallNode("join",
          new JmesPathNode[] {
            new StringNode(", "),
            CurrentNode.instance
          },
          CurrentNode.instance
        )
      )
    };
    Query expected = new Query(
      new CreateObjectNode(pieces,
        new JoinNode(
          new FunctionCallNode("sort",
            new JmesPathNode[] {CurrentNode.instance},
            new JoinNode(
              new PropertyNode("name",
                new SelectionNode(
                  new ComparisonNode("==",
                    new PropertyNode("state", CurrentNode.instance),
                    new StringNode("WA")
                  ),
                  new PropertyNode("locations", CurrentNode.instance)
                )
              )
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    Query expected = new Query(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          CurrentNode.instance
        },
        CurrentNode.instance
      )
    );
    Query actual = AstGenerator.fromString("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Query expected = new Query(
      new CreateArrayNode(
        new JmesPathNode[] {
          new StringNode("bar"),
          CurrentNode.instance
        },
        new PropertyNode("world",
          new JoinNode(
            new PropertyNode("hello", CurrentNode.instance)
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Query expected = new Query(
      new PropertyNode("baz",
        new JoinNode(
          new PropertyNode("bar",
            new JoinNode(
              new PropertyNode("foo", CurrentNode.instance)
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Query expected = new Query(
      new SelectionNode(
        new AndNode(
          new ComparisonNode("==",
            new PropertyNode("bar", CurrentNode.instance),
            new StringNode("baz")
          ),
          new OrNode(
            new ComparisonNode("==",
              new PropertyNode("qux", CurrentNode.instance),
              new StringNode("fux")
            ),
            new ComparisonNode("==",
              new PropertyNode("mux", CurrentNode.instance),
              new StringNode("lux")
            )
          )
        ),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Query expected = new Query(
      new NegateNode(
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Query expected = new Query(
      new SelectionNode(
        new NegateNode(new PropertyNode("bar", CurrentNode.instance)),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?!bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralExpression() {
    Query expected = new Query(new JsonLiteralNode("{}", new HashMap<Object, String>()));
    Query actual = AstGenerator.fromString("`{}`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralArray() {
    Query expected = new Query(new JsonLiteralNode("[]", new ArrayList<Object>()));
    Query actual = AstGenerator.fromString("`[]`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralNumber() {
    Query expected = new Query(new JsonLiteralNode("3.14", new Double(3.14)));
    Query actual = AstGenerator.fromString("`3.14`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralString() {
    Query expected = new Query(new JsonLiteralNode("\"foo\"", new String("foo")));
    Query actual = AstGenerator.fromString("`\"foo\"`");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareJsonLiteralConstant() {
    Query expected = new Query(new JsonLiteralNode("false", false));
    Query actual = AstGenerator.fromString("`false`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void escapedBacktickInJsonString() {
    Query expected = new Query(new JsonLiteralNode("\"fo`o\"", new String("fo`o")));
    Query actual = AstGenerator.fromString("`\"fo\\`o\"`");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore
  public void unEscapedBacktickInJsonString() {
    try {
      AstGenerator.fromString("`\"fo`o\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 5"));
    }
    try {
      AstGenerator.fromString("`\"`foo\"`");
      fail("Expected ParseException to be thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), is("Error while parsing \"`\"fo`o\"`\": unexpected ` at position 3"));
    }
  }

  @Test
  public void comparisonWithJsonLiteralExpression() {
    Query expected = new Query(
      new SelectionNode(
        new ComparisonNode("==",
          new PropertyNode("bar", CurrentNode.instance),
          new JsonLiteralNode("{\"foo\":\"bar\"}", Collections.singletonMap("foo", "bar"))
        ),
        new PropertyNode("foo", CurrentNode.instance)
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    Query expected = new Query(new PropertyNode("false", CurrentNode.instance));
    Query actual = AstGenerator.fromString("false");
    assertThat(actual, is(expected));
  }
}
