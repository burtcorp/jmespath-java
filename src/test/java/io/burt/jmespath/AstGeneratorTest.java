package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import io.burt.jmespath.Query;
import io.burt.jmespath.ast.JmesPathNode;
import io.burt.jmespath.ast.FieldNode;
import io.burt.jmespath.ast.ChainNode;
import io.burt.jmespath.ast.PipeNode;
import io.burt.jmespath.ast.IndexNode;
import io.burt.jmespath.ast.SliceNode;
import io.burt.jmespath.ast.FlattenNode;
import io.burt.jmespath.ast.SelectionNode;
import io.burt.jmespath.ast.SequenceNode;
import io.burt.jmespath.ast.ListWildcardNode;
import io.burt.jmespath.ast.HashWildcardNode;
import io.burt.jmespath.ast.FunctionCallNode;
import io.burt.jmespath.ast.CurrentNodeNode;
import io.burt.jmespath.ast.ComparisonNode;
import io.burt.jmespath.ast.StringNode;
import io.burt.jmespath.ast.AndNode;
import io.burt.jmespath.ast.OrNode;
import io.burt.jmespath.ast.MultiSelectHashNode;
import io.burt.jmespath.ast.MultiSelectListNode;
import io.burt.jmespath.ast.NegationNode;
import io.burt.jmespath.ast.JsonLiteralNode;
import io.burt.jmespath.ast.ExpressionReferenceNode;

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
    Query expected = new Query(new FieldNode("foo"));
    Query actual = AstGenerator.fromString("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() {
    Query expected = new Query(new ChainNode(new ChainNode(new ChainNode(new FieldNode("foo"), new FieldNode("bar")), new FieldNode("baz")), new FieldNode("qux")));
    Query actual = AstGenerator.fromString("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpression() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpression() {
    Query expected = new Query(new PipeNode(new PipeNode(new PipeNode(new FieldNode("foo"), new FieldNode("bar")), new FieldNode("baz")), new FieldNode("qux")));
    Query actual = AstGenerator.fromString("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new IndexNode(3)));
    Query actual = AstGenerator.fromString("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareIndexExpression() {
    Query expected = new Query(new SequenceNode(new IndexNode(3)));
    Query actual = AstGenerator.fromString("[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(3, 4, 1)));
    Query actual = AstGenerator.fromString("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(3, -1, 1)));
    Query actual = AstGenerator.fromString("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(0, 4, 1)));
    Query actual = AstGenerator.fromString("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(3, 4, 5)));
    Query actual = AstGenerator.fromString("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(3, -1, 5)));
    Query actual = AstGenerator.fromString("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(0, -1, 1)));
    Query actual = AstGenerator.fromString("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SliceNode(0, -1, 1)));
    Query actual = AstGenerator.fromString("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSliceExpression() {
    Query expected = new Query(new SequenceNode(new SliceNode(0, 1, 2)));
    Query actual = AstGenerator.fromString("[0:1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void flattenExpression() {
    Query expected = new Query(new FlattenNode(new FieldNode("foo")));
    Query actual = AstGenerator.fromString("foo[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareFlattenExpression() {
    Query expected = new Query(new FlattenNode(new CurrentNodeNode()));
    Query actual = AstGenerator.fromString("[]");
    assertThat(actual, is(expected));
  }

  @Test
  public void listWildcardExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new ListWildcardNode()));
    Query actual = AstGenerator.fromString("foo[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareListWildcardExpression() {
    Query expected = new Query(new ListWildcardNode());
    Query actual = AstGenerator.fromString("[*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void hashWildcardExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new HashWildcardNode()));
    Query actual = AstGenerator.fromString("foo.*");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareHashWildcardExpression() {
    Query expected = new Query(new HashWildcardNode());
    Query actual = AstGenerator.fromString("*");
    assertThat(actual, is(expected));
  }

  @Test
  public void currentNodeExpression() {
    Query expected = new Query(new CurrentNodeNode());
    Query actual = AstGenerator.fromString("@");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SelectionNode(new FieldNode("bar"))));
    Query actual = AstGenerator.fromString("foo[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void selectionWithConditionExpression() {
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SelectionNode(new ComparisonNode("==", new FieldNode("bar"), new FieldNode("baz")))));
    Query actual = AstGenerator.fromString("foo[?bar == baz]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareSelection() {
    Query expected = new Query(new SequenceNode(new SelectionNode(new FieldNode("bar"))));
    Query actual = AstGenerator.fromString("[?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void simpleFunctionCallExpression() {
    Query expected = new Query(new FunctionCallNode("foo"));
    Query actual = AstGenerator.fromString("foo()");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithArgumentExpression() {
    Query expected = new Query(new FunctionCallNode("foo", new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo(bar)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithMultipleArgumentsExpression() {
    Query expected = new Query(new FunctionCallNode("foo", new FieldNode("bar"), new FieldNode("baz"), new CurrentNodeNode()));
    Query actual = AstGenerator.fromString("foo(bar, baz, @)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedFunctionCallExpression() {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new FunctionCallNode("to_string", new CurrentNodeNode())));
    Query actual = AstGenerator.fromString("foo.to_string(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void functionCallWithExpressionReference() {
    Query expected = new Query(
      new FunctionCallNode(
        "foo",
        new ExpressionReferenceNode(
          new ChainNode(
            new FieldNode("bar"),
            new FieldNode("bar")
          )
        )
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
    Query expected = new Query(new SequenceNode(new FieldNode("foo"), new SelectionNode(new ComparisonNode("!=", new FieldNode("bar"), new StringNode("baz")))));
    Query actual = AstGenerator.fromString("foo[?bar != 'baz']");
    assertThat(actual, is(expected));
  }

  @Test
  public void andExpression() {
    Query expected = new Query(new AndNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo && bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void orExpression() {
    Query expected = new Query(new OrNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo || bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void implicitCurrentNodeBeforeListWildcard() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new ListWildcardNode()));
    Query actual = AstGenerator.fromString("foo | [*]");
    assertThat(actual, is(expected));
  }

  @Test
  public void implicitCurrentNodeBeforeIndex() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new SequenceNode(new IndexNode(1))));
    Query actual = AstGenerator.fromString("foo | [1]");
    assertThat(actual, is(expected));
  }

  @Test
  public void implicitCurrentNodeBeforeSlice() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new SequenceNode(new SliceNode(1, 2, 1))));
    Query actual = AstGenerator.fromString("foo | [1:2]");
    assertThat(actual, is(expected));
  }

  @Test
  public void implicitCurrentNodeBeforeFlatten() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new FlattenNode(new CurrentNodeNode())));
    Query actual = AstGenerator.fromString("foo | []");
    assertThat(actual, is(expected));
  }

  @Test
  public void implicitCurrentNodeBeforeSelection() {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new SequenceNode(new SelectionNode(new FieldNode("bar")))));
    Query actual = AstGenerator.fromString("foo | [?bar]");
    assertThat(actual, is(expected));
  }

  @Test
  public void booleanComparisonExpression() {
    Query expected = new Query(
      new SequenceNode(
        new FieldNode("foo"),
        new SelectionNode(
          new OrNode(
            new AndNode(
              new ComparisonNode("!=", new FieldNode("bar"), new StringNode("baz")),
              new ComparisonNode("==", new FieldNode("qux"), new StringNode("fux"))
            ),
            new ComparisonNode(">", new FieldNode("mux"), new StringNode("lux"))
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar != 'baz' && qux == 'fux' || mux > 'lux']");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeFunctionCallCombination() {
    Query expected = new Query(
      new PipeNode(
        new FlattenNode(
          new ChainNode(
            new FieldNode("foo"),
            new FieldNode("bar")
          )
        ),
        new FunctionCallNode(
          "sort",
          new CurrentNodeNode()
        )
      )
    );
    Query actual = AstGenerator.fromString("foo.bar[] | sort(@)");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() {
    Query expected = new Query(
      new PipeNode(
        new ChainNode(
          new SequenceNode(new FieldNode("foo"), new IndexNode(3)),
          new FieldNode("bar")
        ),
        new SequenceNode(
          new ChainNode(
            new FieldNode("baz"),
            new FieldNode("qux")
          ),
          new SliceNode(2, 3, 1)
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[3].bar | baz.qux[2:3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectHashExpression() {
    Map<String, JmesPathNode> kvs = new HashMap<>();
    kvs.put("foo", new StringNode("bar"));
    kvs.put("baz", new CurrentNodeNode());
    Query expected = new Query(new MultiSelectHashNode(kvs));
    Query actual = AstGenerator.fromString("{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectHashExpression() {
    Map<String, JmesPathNode> kvs = new HashMap<>();
    kvs.put("foo", new StringNode("bar"));
    kvs.put("baz", new CurrentNodeNode());
    Query expected = new Query(
      new PipeNode(
        new FieldNode("hello"),
        new ChainNode(
          new FieldNode("world"),
          new MultiSelectHashNode(kvs)
        )
      )
    );
    Query actual = AstGenerator.fromString("hello | world.{foo: 'bar', baz: @}");
    assertThat(actual, is(expected));
  }

  @Test
  public void jmesPathSiteExampleExpression() {
    Query expected = new Query(
      new PipeNode(
        new PipeNode(
          new ChainNode(
            new SequenceNode(
              new FieldNode("locations"),
              new SelectionNode(
                new ComparisonNode("==", new FieldNode("state"), new StringNode("WA"))
              )
            ),
            new FieldNode("name")
          ),
          new FunctionCallNode("sort", new CurrentNodeNode())
        ),
        new MultiSelectHashNode(
          Collections.singletonMap(
            "WashingtonCities",
            (JmesPathNode) new FunctionCallNode("join", new StringNode(", "), new CurrentNodeNode())
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareMultiSelectListExpression() {
    Query expected = new Query(new MultiSelectListNode(new StringNode("bar"), new CurrentNodeNode()));
    Query actual = AstGenerator.fromString("['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainedMultiSelectListExpression() {
    Query expected = new Query(
      new PipeNode(
        new FieldNode("hello"),
        new ChainNode(
          new FieldNode("world"),
          new MultiSelectListNode(new StringNode("bar"), new CurrentNodeNode())
        )
      )
    );
    Query actual = AstGenerator.fromString("hello | world.['bar', @]");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedPipeExpression() {
    Query expected = new Query(
      new PipeNode(
        new FieldNode("foo"),
        new PipeNode(
          new FieldNode("bar"),
          new FieldNode("baz")
        )
      )
    );
    Query actual = AstGenerator.fromString("foo | (bar | baz)");
    assertThat(actual, is(expected));
  }

  @Test
  public void parenthesizedComparisonExpression() {
    Query expected = new Query(
      new SequenceNode(
        new FieldNode("foo"),
        new SelectionNode(
          new AndNode(
            new ComparisonNode("==", new FieldNode("bar"), new StringNode("baz")),
            new OrNode(
              new ComparisonNode("==", new FieldNode("qux"), new StringNode("fux")),
              new ComparisonNode("==", new FieldNode("mux"), new StringNode("lux"))
            )
          )
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar == 'baz' && (qux == 'fux' || mux == 'lux')]");
    assertThat(actual, is(expected));
  }

  @Test
  public void bareNegatedExpression() {
    Query expected = new Query(
      new NegationNode(
        new FieldNode("foo")
      )
    );
    Query actual = AstGenerator.fromString("!foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void negatedSelectionExpression() {
    Query expected = new Query(
      new SequenceNode(
        new FieldNode("foo"),
        new SelectionNode(
          new NegationNode(
            new FieldNode("bar")
          )
        )
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
      new SequenceNode(
        new FieldNode("foo"),
        new SelectionNode(
          new ComparisonNode("==", new FieldNode("bar"), new JsonLiteralNode("{\"foo\":\"bar\"}", Collections.singletonMap("foo", "bar")))
        )
      )
    );
    Query actual = AstGenerator.fromString("foo[?bar == `{\"foo\": \"bar\"}`]");
    assertThat(actual, is(expected));
  }

  @Test
  @Ignore("Known issue")
  public void jsonBuiltinsAsNames() {
    Query expected = new Query(new FieldNode("false"));
    Query actual = AstGenerator.fromString("false");
    assertThat(actual, is(expected));
  }
}
