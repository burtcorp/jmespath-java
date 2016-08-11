package io.burt.jmespath.parser;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Expression;
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

public class ExpressionParser<T> extends JmesPathBaseVisitor<JmesPathNode<T>> {
  private final ParseTree tree;
  private final Deque<JmesPathNode<T>> currentSource;
  private final Adapter<T> runtime;

  public static <U> Expression<U> fromString(Adapter<U> runtime, String expression) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(expression), errors), errors);
    ParseTree tree = parser.jmesPathExpression();
    if (errors.isEmpty()) {
      ExpressionParser<U> visitor = new ExpressionParser<U>(runtime, tree);
      return visitor.expression();
    } else {
      throw new ParseException(expression, errors);
    }
  }

  private static ANTLRInputStream createInput(String expression) {
    return new ANTLRInputStream(expression);
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

  private ExpressionParser(Adapter<T> runtime, ParseTree tree) {
    this.runtime = runtime;
    this.tree = tree;
    this.currentSource = new LinkedList<>();
  }

  public Expression<T> expression() {
    return visit(tree);
  }

  private String identifierToString(JmesPathParser.IdentifierContext ctx) {
    String id = ctx.getText();
    if (ctx.STRING() != null) {
      id = id.substring(1, id.length() - 1);
    }
    return id;
  }

  @Override
  public JmesPathNode<T> visitJmesPathExpression(JmesPathParser.JmesPathExpressionContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    JmesPathNode<T> result = visit(ctx.expression());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode<T> visitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    currentSource.push(new JoinNode<T>(runtime, visit(ctx.expression(0))));
    JmesPathNode<T> result = visit(ctx.expression(1));
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode<T> visitIdentifierExpression(JmesPathParser.IdentifierExpressionContext ctx) {
    return visit(ctx.identifier());
  }

  @Override
  public JmesPathNode<T> visitNotExpression(JmesPathParser.NotExpressionContext ctx) {
    return new NegateNode<T>(runtime, visit(ctx.expression()));
  }

  @Override
  public JmesPathNode<T> visitRawStringExpression(JmesPathParser.RawStringExpressionContext ctx) {
    String quotedString = ctx.RAW_STRING().getText();
    String unquotedString = quotedString.substring(1, quotedString.length() - 1);
    return new StringNode<T>(runtime, unquotedString);
  }

  @Override
  public JmesPathNode<T> visitComparisonExpression(JmesPathParser.ComparisonExpressionContext ctx) {
    String operator = ctx.COMPARATOR().getText();
    JmesPathNode<T> left = visit(ctx.expression(0));
    JmesPathNode<T> right = visit(ctx.expression(1));
    return new ComparisonNode<T>(runtime, operator, left, right);
  }

  @Override
  public JmesPathNode<T> visitParenExpression(JmesPathParser.ParenExpressionContext ctx) {
    return visit(ctx.expression());
  }

  @Override
  public JmesPathNode<T> visitBracketExpression(JmesPathParser.BracketExpressionContext ctx) {
    return visit(ctx.bracketSpecifier());
  }

  @Override
  public JmesPathNode<T> visitOrExpression(JmesPathParser.OrExpressionContext ctx) {
    JmesPathNode<T> left = visit(ctx.expression(0));
    JmesPathNode<T> right = visit(ctx.expression(1));
    return new OrNode<T>(runtime, left, right);

  }

  @Override
  public JmesPathNode<T> visitChainExpression(JmesPathParser.ChainExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    JmesPathNode<T> result = visit(ctx.chainedExpression());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode<T> visitAndExpression(JmesPathParser.AndExpressionContext ctx) {
    JmesPathNode<T> left = visit(ctx.expression(0));
    JmesPathNode<T> right = visit(ctx.expression(1));
    return new AndNode<T>(runtime, left, right);
  }

  @Override
  public JmesPathNode<T> visitWildcardExpression(JmesPathParser.WildcardExpressionContext ctx) {
    return visit(ctx.wildcard());
  }

  @Override
  public JmesPathNode<T> visitBracketedExpression(JmesPathParser.BracketedExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    JmesPathNode<T> result = visit(ctx.bracketSpecifier());
    currentSource.pop();
    return result;
  }

  @Override
  public JmesPathNode<T> visitWildcard(JmesPathParser.WildcardContext ctx) {
    return new ForkNode<T>(runtime, new FlattenObjectNode<T>(runtime, currentSource.peek()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public JmesPathNode<T> visitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    int n = ctx.expression().size();
    List<JmesPathNode<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      entries.add(visit(ctx.expression(i)));
    }
    currentSource.pop();
    return new CreateArrayNode<T>(runtime, entries, currentSource.peek());
  }

  @Override
  @SuppressWarnings("unchecked")
  public JmesPathNode<T> visitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    int n = ctx.keyvalExpr().size();
    List<CreateObjectNode.Entry<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      JmesPathParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
      String key = identifierToString(kvCtx.identifier());
      JmesPathNode<T> value = visit(kvCtx.expression());
      entries.add(new CreateObjectNode.Entry<T>(key, value));
    }
    currentSource.pop();
    return new CreateObjectNode<T>(runtime, entries, currentSource.peek());
  }

  @Override
  public JmesPathNode<T> visitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    return new IndexNode<T>(runtime, index, currentSource.peek());
  }

  @Override
  public JmesPathNode<T> visitBracketStar(JmesPathParser.BracketStarContext ctx) {
    return new ForkNode<T>(runtime, currentSource.peek());
  }

  @Override
  public JmesPathNode<T> visitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
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
    return new SliceNode<T>(runtime, start, stop, step, currentSource.peek());
  }

  @Override
  public JmesPathNode<T> visitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    return new ForkNode<T>(runtime, new FlattenArrayNode<T>(runtime, currentSource.peek()));
  }

  @Override
  public JmesPathNode<T> visitSelect(JmesPathParser.SelectContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    JmesPathNode<T> test = visit(ctx.expression());
    currentSource.pop();
    return new ForkNode<T>(runtime, new SelectionNode<T>(runtime, test, currentSource.peek()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public JmesPathNode<T> visitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    currentSource.push(new CurrentNode(runtime));
    String name = ctx.NAME().getText();
    int n = ctx.functionArg().size();
    List<JmesPathNode<T>> args = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      args.add(visit(ctx.functionArg(i)));
    }
    currentSource.pop();
    return new FunctionCallNode<T>(runtime, name, args, currentSource.peek());
  }

  @Override
  public JmesPathNode<T> visitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    if (currentSource.peek() instanceof CurrentNode) {
      return currentSource.peek();
    } else {
      return new CurrentNode<T>(runtime, currentSource.peek());
    }
  }

  @Override
  public JmesPathNode<T> visitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    return new ExpressionReferenceNode<T>(runtime, visit(ctx.expression()));
  }

  @Override
  public JmesPathNode<T> visitLiteral(JmesPathParser.LiteralContext ctx) {
    String string = ctx.jsonValue().getText();
    return new ParsedJsonLiteralNode<T>(runtime, string, runtime.parseString(string));
  }

  @Override
  public JmesPathNode<T> visitIdentifier(JmesPathParser.IdentifierContext ctx) {
    return new PropertyNode<T>(runtime, identifierToString(ctx), currentSource.peek());
  }
}
