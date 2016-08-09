package io.burt.jmespath.parser;

import java.util.Deque;
import java.util.LinkedList;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Query;
import io.burt.jmespath.Adapter;
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

public class JmesPathQueryParser<T> extends JmesPathBaseVisitor<JmesPathNode> {
  private final ParseTree tree;
  private final Deque<JmesPathNode> currentSource;
  private final Adapter<T> adapter;

  public static Query fromString(String query) {
    return fromString(query, null);
  }

  public static <T> Query fromString(String query, Adapter<T> adapter) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(query), errors), errors);
    ParseTree tree = parser.query();
    if (errors.isEmpty()) {
      JmesPathQueryParser<T> visitor = new JmesPathQueryParser<T>(tree, adapter);
      return visitor.query();
    } else {
      throw new ParseException(query, errors);
    }
  }

  private static ANTLRInputStream createInput(String query) {
    return new ANTLRInputStream(query);
  }

  private static JmesPathLexer createLexer(ANTLRInputStream input, ANTLRErrorListener errorListener) {
    JmesPathLexer lexer = new JmesPathLexer(input);
    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
    lexer.addErrorListener(errorListener);
    return lexer;
  }

  private static JmesPathParser createParser(JmesPathLexer lexer, ANTLRErrorListener errorListener) {
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JmesPathParser parser = new JmesPathParser(tokens);
    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    return parser;
  }

  private JmesPathQueryParser(ParseTree tree, Adapter<T> adapter) {
    this.tree = tree;
    this.currentSource = new LinkedList<>();
    this.adapter = adapter;
  }

  public Query query() {
    return new Query(visit(tree));
  }

  private String identifierToString(JmesPathParser.IdentifierContext ctx) {
    String id = ctx.getText();
    if (ctx.STRING() != null) {
      id = id.substring(1, id.length() - 1);
    }
    return id;
  }

  @Override
  public JmesPathNode visitQuery(JmesPathParser.QueryContext ctx) {
    currentSource.push(new CurrentNode());
    JmesPathNode result = visit(ctx.expression());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode visitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    currentSource.push(new JoinNode(visit(ctx.expression(0))));
    JmesPathNode result = visit(ctx.expression(1));
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode visitIdentifierExpression(JmesPathParser.IdentifierExpressionContext ctx) {
    return visit(ctx.identifier());
  }

  @Override
  public JmesPathNode visitNotExpression(JmesPathParser.NotExpressionContext ctx) {
    return new NegateNode(visit(ctx.expression()));
  }

  @Override
  public JmesPathNode visitRawStringExpression(JmesPathParser.RawStringExpressionContext ctx) {
    String quotedString = ctx.RAW_STRING().getText();
    String unquotedString = quotedString.substring(1, quotedString.length() - 1);
    return new StringNode(unquotedString);
  }

  @Override
  public JmesPathNode visitComparisonExpression(JmesPathParser.ComparisonExpressionContext ctx) {
    String operator = ctx.COMPARATOR().getText();
    JmesPathNode left = visit(ctx.expression(0));
    JmesPathNode right = visit(ctx.expression(1));
    return new ComparisonNode(operator, left, right);
  }

  @Override
  public JmesPathNode visitParenExpression(JmesPathParser.ParenExpressionContext ctx) {
    return visit(ctx.expression());
  }

  @Override
  public JmesPathNode visitBracketExpression(JmesPathParser.BracketExpressionContext ctx) {
    return visit(ctx.bracketSpecifier());
  }

  @Override
  public JmesPathNode visitOrExpression(JmesPathParser.OrExpressionContext ctx) {
    JmesPathNode left = visit(ctx.expression(0));
    JmesPathNode right = visit(ctx.expression(1));
    return new OrNode(left, right);

  }

  @Override
  public JmesPathNode visitChainExpression(JmesPathParser.ChainExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    JmesPathNode result = visit(ctx.chainedExpression());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode visitAndExpression(JmesPathParser.AndExpressionContext ctx) {
    JmesPathNode left = visit(ctx.expression(0));
    JmesPathNode right = visit(ctx.expression(1));
    return new AndNode(left, right);
  }

  @Override
  public JmesPathNode visitWildcardExpression(JmesPathParser.WildcardExpressionContext ctx) {
    return visit(ctx.wildcard());
  }

  @Override
  public JmesPathNode visitBracketedExpression(JmesPathParser.BracketedExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    JmesPathNode result = visit(ctx.bracketSpecifier());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode visitWildcard(JmesPathParser.WildcardContext ctx) {
    return new ForkNode(new FlattenObjectNode(currentSource.peek()));
  }

  @Override
  public JmesPathNode visitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    currentSource.push(new CurrentNode());
    int n = ctx.expression().size();
    JmesPathNode[] entries = new JmesPathNode[n];
    for (int i = 0; i < n; i++) {
      entries[i] = visit(ctx.expression(i));
    }
    currentSource.pop();
    return new CreateArrayNode(entries, currentSource.peek());
  }

  @Override
  public JmesPathNode visitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    currentSource.push(new CurrentNode());
    int n = ctx.keyvalExpr().size();
    CreateObjectNode.Entry[] entries = new CreateObjectNode.Entry[n];
    for (int i = 0; i < n; i++) {
      JmesPathParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
      String key = identifierToString(kvCtx.identifier());
      JmesPathNode value = visit(kvCtx.expression());
      entries[i] = new CreateObjectNode.Entry(key, value);
    }
    currentSource.pop();
    return new CreateObjectNode(entries, currentSource.peek());
  }

  @Override
  public JmesPathNode visitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    return new IndexNode(index, currentSource.peek());
  }

  @Override
  public JmesPathNode visitBracketStar(JmesPathParser.BracketStarContext ctx) {
    return new ForkNode(currentSource.peek());
  }

  @Override
  public JmesPathNode visitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
    int start = 0;
    int stop = 0;
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
    return new SliceNode(start, stop, step, currentSource.peek());
  }

  @Override
  public JmesPathNode visitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    return new ForkNode(new FlattenArrayNode(currentSource.peek()));
  }

  @Override
  public JmesPathNode visitSelect(JmesPathParser.SelectContext ctx) {
    currentSource.push(new CurrentNode());
    JmesPathNode test = visit(ctx.expression());
    currentSource.pop();
    return new ForkNode(new SelectionNode(test, currentSource.peek()));
  }

  @Override
  public JmesPathNode visitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    currentSource.push(new CurrentNode());
    String name = ctx.NAME().getText();
    int n = ctx.functionArg().size();
    JmesPathNode[] args = new JmesPathNode[n];
    for (int i = 0; i < n; i++) {
      args[i] = visit(ctx.functionArg(i));
    }
    currentSource.pop();
    return new FunctionCallNode(name, args, currentSource.peek());
  }

  @Override
  public JmesPathNode visitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    if (currentSource.peek() instanceof CurrentNode) {
      return currentSource.peek();
    } else {
      return new CurrentNode(currentSource.peek());
    }
  }

  @Override
  public JmesPathNode visitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    return new ExpressionReferenceNode(visit(ctx.expression()));
  }

  @Override
  public JmesPathNode visitLiteral(JmesPathParser.LiteralContext ctx) {
    String string = ctx.jsonValue().getText();
    if (adapter != null) {
      return new ParsedJsonLiteralNode(string, adapter.parseString(string));
    } else {
      return new JsonLiteralNode(string);
    }
  }

  @Override
  public JmesPathNode visitIdentifier(JmesPathParser.IdentifierContext ctx) {
    return new PropertyNode(identifierToString(ctx), currentSource.peek());
  }
}
