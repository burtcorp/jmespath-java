package io.burt.jmespath;

import java.util.Deque;
import java.util.LinkedList;

import io.burt.jmespath.JmesPathParser;
import io.burt.jmespath.JmesPathBaseListener;
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

public class AstGeneratingListener extends JmesPathBaseListener {
  private final Deque<JmesPathNode> stack;
  private Query query;

  public AstGeneratingListener() {
    this.stack = new LinkedList<>();
    this.query = null;
  }

  public Query ast() {
    return query;
  }

  @Override
  public void exitQuery(JmesPathParser.QueryContext ctx) {
    JmesPathNode expression = stack.pop();
    query = new Query(expression);
  }

  @Override
  public void exitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    JmesPathNode right = stack.pop();
    JmesPathNode left = stack.pop();
    stack.push(new PipeNode(left, right));
  }

  @Override
  public void exitIdentifierExpression(JmesPathParser.IdentifierExpressionContext ctx) {
    stack.push(new FieldNode(ctx.identifier().getText()));
  }

  @Override
  public void exitChainExpression(JmesPathParser.ChainExpressionContext ctx) {
    JmesPathNode left = stack.pop();
    JmesPathNode right;
    if (ctx.identifier() != null) {
      right = new FieldNode(ctx.identifier().getText());
    } else {
      throw new UnsupportedOperationException("ChainNode with non-identifier not supported");
    }
    stack.push(new ChainNode(left, right));
  }

  @Override
  public void exitBracketedExpression(JmesPathParser.BracketedExpressionContext ctx) {
    JmesPathNode right;
    JmesPathParser.BracketSpecifierContext bracketSpecCtx = ctx.bracketSpecifier();
    if (bracketSpecCtx.slice() != null) {
      JmesPathParser.SliceContext sliceCtx = ctx.bracketSpecifier().slice();
      int start = 0;
      int stop = -1;
      int step = 1;
      if (sliceCtx.start != null) {
        start = Integer.parseInt(sliceCtx.start.getText());
      }
      if (sliceCtx.stop != null) {
        stop = Integer.parseInt(sliceCtx.stop.getText());
      }
      if (sliceCtx.step != null) {
        step = Integer.parseInt(sliceCtx.step.getText());
      }
      right = new SliceNode(start, stop, step);
    } else if (bracketSpecCtx.SIGNED_INT() != null) {
      int index = Integer.parseInt(bracketSpecCtx.SIGNED_INT().getText());
      right = new IndexNode(index);
    } else if (bracketSpecCtx.expression() != null) {
      JmesPathNode test = stack.pop();
      right = new SelectionNode(test);
    } else {
      right = new FlattenNode();
    }
    JmesPathNode left = stack.pop();
    stack.push(new SequenceNode(left, right));
  }

  @Override
  public void exitBracketExpression(JmesPathParser.BracketExpressionContext ctx) {
    stack.push(new FlattenNode());
  }
}
