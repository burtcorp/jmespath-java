package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import io.burt.jmespath.Query;
import io.burt.jmespath.ast.FieldNode;
import io.burt.jmespath.ast.ChainNode;
import io.burt.jmespath.ast.PipeNode;
import io.burt.jmespath.ast.IndexNode;
import io.burt.jmespath.ast.SliceNode;

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
  public void identifierExpression() throws IOException {
    Query expected = new Query(new FieldNode("foo"));
    Query actual = AstGenerator.fromString("foo");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo.bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longChainExpression() throws IOException {
    Query expected = new Query(new ChainNode(new ChainNode(new ChainNode(new FieldNode("foo"), new FieldNode("bar")), new FieldNode("baz")), new FieldNode("qux")));
    Query actual = AstGenerator.fromString("foo.bar.baz.qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void pipeExpression() throws IOException {
    Query expected = new Query(new PipeNode(new FieldNode("foo"), new FieldNode("bar")));
    Query actual = AstGenerator.fromString("foo | bar");
    assertThat(actual, is(expected));
  }

  @Test
  public void longPipeExpression() throws IOException {
    Query expected = new Query(new PipeNode(new PipeNode(new PipeNode(new FieldNode("foo"), new FieldNode("bar")), new FieldNode("baz")), new FieldNode("qux")));
    Query actual = AstGenerator.fromString("foo | bar | baz | qux");
    assertThat(actual, is(expected));
  }

  @Test
  public void indexExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new IndexNode(3)));
    Query actual = AstGenerator.fromString("foo[3]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(3, 4, 1)));
    Query actual = AstGenerator.fromString("foo[3:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStopExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(3, -1, 1)));
    Query actual = AstGenerator.fromString("foo[3:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithoutStartExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(0, 4, 1)));
    Query actual = AstGenerator.fromString("foo[:4]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(3, 4, 5)));
    Query actual = AstGenerator.fromString("foo[3:4:5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithStepButWithoutStopExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(3, -1, 5)));
    Query actual = AstGenerator.fromString("foo[3::5]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustColonExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(0, -1, 1)));
    Query actual = AstGenerator.fromString("foo[:]");
    assertThat(actual, is(expected));
  }

  @Test
  public void sliceWithJustTwoColonsExpression() throws IOException {
    Query expected = new Query(new ChainNode(new FieldNode("foo"), new SliceNode(0, -1, 1)));
    Query actual = AstGenerator.fromString("foo[::]");
    assertThat(actual, is(expected));
  }

  @Test
  public void chainPipeIndexSliceCombination() throws IOException {
    Query expected = new Query(
      new PipeNode(
        new ChainNode(
          new ChainNode(new FieldNode("foo"), new IndexNode(3)),
          new FieldNode("bar")
        ),
        new ChainNode(
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
}
