package io.burt.jmespath;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;

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

public class AstGeneratingListener extends JmesPathBaseListener {
  private final Deque<JmesPathNode> stack;
  private final Deque<Object> jsonStack;
  private Query query;

  public AstGeneratingListener() {
    this.stack = new LinkedList<>();
    this.jsonStack = new LinkedList<>();
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
    JmesPathNode right;
    if (ctx.identifier() != null) {
      right = new FieldNode(ctx.identifier().getText());
    } else if (ctx.wildcard != null) {
      right = new HashWildcardNode();
    } else {
      right = stack.pop();
    }
    JmesPathNode left = stack.pop();
    stack.push(new ChainNode(left, right));
  }

  @Override
  public void exitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    JmesPathNode right = new IndexNode(index);
    if (ctx.getParent() instanceof JmesPathParser.BracketExpressionContext) {
      stack.push(new SequenceNode(right));
    } else {
      JmesPathNode left = stack.pop();
      stack.push(new SequenceNode(left, right));
    }
  }

  @Override
  public void exitBracketStar(JmesPathParser.BracketStarContext ctx) {
    if (ctx.getParent() instanceof JmesPathParser.BracketExpressionContext) {
      stack.push(new ListWildcardNode());
    } else {
      JmesPathNode left = stack.pop();
      stack.push(new SequenceNode(left, new ListWildcardNode()));
    }
  }

  @Override
  public void exitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
    int start = 0;
    int stop = -1;
    int step = 1;
    JmesPathParser.SliceContext sliceCtx = ctx.slice();
    if (sliceCtx.start != null) {
      start = Integer.parseInt(sliceCtx.start.getText());
    }
    if (sliceCtx.stop != null) {
      stop = Integer.parseInt(sliceCtx.stop.getText());
    }
    if (sliceCtx.step != null) {
      step = Integer.parseInt(sliceCtx.step.getText());
    }
    JmesPathNode right = new SliceNode(start, stop, step);
    if (ctx.getParent() instanceof JmesPathParser.BracketExpressionContext) {
      stack.push(new SequenceNode(right));
    } else {
     JmesPathNode left = stack.pop();
     stack.push(new SequenceNode(left, right));
    }
  }

  @Override
  public void exitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    if (ctx.getParent() instanceof JmesPathParser.BracketExpressionContext) {
      stack.push(new FlattenNode(new CurrentNodeNode()));
    } else {
      JmesPathNode expression = stack.pop();
      stack.push(new FlattenNode(expression));
    }
  }

  @Override
  public void exitSelect(JmesPathParser.SelectContext ctx) {
    JmesPathNode test = stack.pop();
    JmesPathNode right = new SelectionNode(test);
    if (ctx.getParent() instanceof JmesPathParser.BracketExpressionContext) {
      stack.push(new SequenceNode(right));
    } else {
      JmesPathNode left = stack.pop();
      stack.push(new SequenceNode(left, right));
    }
  }

  @Override
  public void exitComparisonExpression(JmesPathParser.ComparisonExpressionContext ctx) {
    String operator = ctx.COMPARATOR().getText();
    JmesPathNode right = stack.pop();
    JmesPathNode left = stack.pop();
    stack.push(new ComparisonNode(operator, left, right));
  }

  @Override
  public void exitWildcardExpression(JmesPathParser.WildcardExpressionContext ctx) {
    stack.push(new HashWildcardNode());
  }

  @Override
  public void exitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    JmesPathNode[] args;
    if (ctx.noArgs() != null) {
      args = new JmesPathNode[] {};
    } else {
      int n = ctx.oneOrMoreArgs().functionArg().size();
      args = new JmesPathNode[n];
      for (int i = n - 1; i >= 0; i--) {
        args[i] = stack.pop();
      }
    }
    String name = ctx.NAME().getText();
    stack.push(new FunctionCallNode(name, args));
  }

  @Override
  public void exitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    stack.push(new CurrentNodeNode());
  }

  @Override
  public void exitRawStringExpression(JmesPathParser.RawStringExpressionContext ctx) {
    String quotedString = ctx.RAW_STRING().getText();
    stack.push(new StringNode(quotedString.substring(1, quotedString.length() - 1)));
  }

  @Override
  public void exitAndExpression(JmesPathParser.AndExpressionContext ctx) {
    JmesPathNode right = stack.pop();
    JmesPathNode left = stack.pop();
    stack.push(new AndNode(left, right));
  }

  @Override
  public void exitOrExpression(JmesPathParser.OrExpressionContext ctx) {
    JmesPathNode right = stack.pop();
    JmesPathNode left = stack.pop();
    stack.push(new OrNode(left, right));
  }

  @Override
  public void exitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    int n = ctx.keyvalExpr().size();
    MultiSelectHashNode.KV[] kvs = new MultiSelectHashNode.KV[n];
    for (int i = n - 1; i >= 0; i--) {
      kvs[i] = (MultiSelectHashNode.KV) stack.pop();
    }
    stack.push(new MultiSelectHashNode(kvs));
  }

  @Override
  public void exitKeyvalExpr(JmesPathParser.KeyvalExprContext ctx) {
    String key = ctx.identifier().getText();
    JmesPathNode value = stack.pop();
    stack.push(new MultiSelectHashNode.KV(key, value));
  }

  @Override
  public void exitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    int n = ctx.expression().size();
    JmesPathNode[] elements = new JmesPathNode[n];
    for (int i = n - 1; i >= 0; i--) {
      elements[i] = stack.pop();
    }
    stack.push(new MultiSelectListNode(elements));
  }

  @Override
  public void exitNotExpression(JmesPathParser.NotExpressionContext ctx) {
    JmesPathNode expression = stack.pop();
    stack.push(new NegationNode(expression));
  }

  @Override
  public void exitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    JmesPathNode expression = stack.pop();
    stack.push(new ExpressionReferenceNode(expression));
  }

  @Override
  public void exitLiteral(JmesPathParser.LiteralContext ctx) {
    String raw = ctx.jsonValue().getText();
    Object tree = jsonStack.pop();
    stack.push(new JsonLiteralNode(raw, tree));
  }

  @Override
  public void exitJsonStringValue(JmesPathParser.JsonStringValueContext ctx) {
    String str = ctx.STRING().getText();
    jsonStack.push(str.substring(1, str.length() - 1));
  }

  @Override
  public void exitJsonNumberValue(JmesPathParser.JsonNumberValueContext ctx) {
    jsonStack.push(Double.parseDouble(ctx.getText()));
  }

  @Override
  public void exitJsonObjectPair(JmesPathParser.JsonObjectPairContext ctx) {
    String str = ctx.STRING().getText();
    jsonStack.push(str.substring(1, str.length() - 1));
  }

  @Override
  public void exitJsonObjectValue(JmesPathParser.JsonObjectValueContext ctx) {
    int n = ctx.jsonObject().jsonObjectPair().size();
    Map<String, Object> object = new LinkedHashMap<>(n);
    for (int i = n - 1; i >= 0; i--) {
      String key = (String) jsonStack.pop();
      Object value = jsonStack.pop();
      object.put(key, value);
    }
    jsonStack.push(object);
  }

  @Override
  public void exitJsonArrayValue(JmesPathParser.JsonArrayValueContext ctx) {
    int n = ctx.jsonArray().jsonValue().size();
    List<Object> array = new ArrayList<>(n);
    for (int i = n - 1; i >= 0; i--) {
      Object value = jsonStack.pop();
      array.add(value);
    }
    jsonStack.push(array);
  }

  @Override
  public void exitJsonConstantValue(JmesPathParser.JsonConstantValueContext ctx) {
    if (ctx.t != null) {
      jsonStack.push(true);
    } else if (ctx.f != null) {
      jsonStack.push(false);
    } else {
      jsonStack.push(null);
    }
  }
}
