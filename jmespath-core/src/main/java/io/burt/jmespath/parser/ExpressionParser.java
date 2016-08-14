package io.burt.jmespath.parser;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Arrays;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Expression;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.util.StringEscapeHelper;
import io.burt.jmespath.function.Function;
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
import io.burt.jmespath.node.Node;
import io.burt.jmespath.node.JoinNode;
import io.burt.jmespath.node.JsonLiteralNode;
import io.burt.jmespath.node.NegateNode;
import io.burt.jmespath.node.OrNode;
import io.burt.jmespath.node.PropertyNode;
import io.burt.jmespath.node.SelectionNode;
import io.burt.jmespath.node.SliceNode;
import io.burt.jmespath.node.StringNode;

public class ExpressionParser<T> extends JmesPathBaseVisitor<Node<T>> {
  private static final StringEscapeHelper identifierEscapeHelper = new StringEscapeHelper(
    true,
    '"', '"',
    '/', '/',
    '\\', '\\',
    'b', '\b',
    'f', '\f',
    'n', '\n',
    'r', '\r',
    't', '\t'
  );

  private static final StringEscapeHelper rawStringEscapeHelper = new StringEscapeHelper(
    false,
    '\'', '\'',
    '\\', '\\'
  );

  private final ParseTree tree;
  private final Deque<Node<T>> currentSource;
  private final Adapter<T> runtime;
  private final ParseErrorAccumulator errors;

  public static <U> Expression<U> fromString(Adapter<U> runtime, String rawExpression) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(rawExpression), errors), errors);
    ParseTree tree = parser.jmesPathExpression();
    Expression<U> expression = null;
    if (errors.isEmpty()) {
      ExpressionParser<U> visitor = new ExpressionParser<U>(runtime, tree, errors);
      expression = visitor.expression();
    }
    if (!errors.isEmpty()) {
      throw new ParseException(rawExpression, errors);
    }
    return expression;
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

  private ExpressionParser(Adapter<T> runtime, ParseTree tree, ParseErrorAccumulator errors) {
    this.runtime = runtime;
    this.tree = tree;
    this.errors = errors;
    this.currentSource = new LinkedList<>();
  }

  public Expression<T> expression() {
    return visit(tree);
  }

  private String identifierToString(JmesPathParser.IdentifierContext ctx) {
    String id = ctx.getText();
    if (ctx.STRING() != null) {
      id = identifierEscapeHelper.unescape(id.substring(1, id.length() - 1));
    }
    return id;
  }

  private void checkForUnescapedBackticks(Token token) {
    int unescapedBacktickIndex = indexOfUnescapedBacktick(token.getText());
    if (unescapedBacktickIndex > -1) {
      errors.parseError("unexpected `", token.getLine(), token.getStartIndex() + unescapedBacktickIndex);
    }
  }

  private int indexOfUnescapedBacktick(String str) {
    int backtickIndex = str.indexOf('`');
    while (backtickIndex > -1) {
      if (backtickIndex == 0 || str.charAt(backtickIndex - 1) != '\\') {
        return backtickIndex;
      }
      backtickIndex = str.indexOf('`', backtickIndex + 1);
    }
    return -1;
  }

  @Override
  public Node<T> visitJmesPathExpression(JmesPathParser.JmesPathExpressionContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    Node<T> result = visit(ctx.expression());
    currentSource.pop();
    return result;
  }

  @Override
  public Node<T> visitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    currentSource.push(new JoinNode<T>(runtime, visit(ctx.expression(0))));
    Node<T> result = visit(ctx.expression(1));
    currentSource.pop();
    return result;
  }

  @Override
  public Node<T> visitIdentifierExpression(JmesPathParser.IdentifierExpressionContext ctx) {
    return visit(ctx.identifier());
  }

  @Override
  public Node<T> visitNotExpression(JmesPathParser.NotExpressionContext ctx) {
    return new NegateNode<T>(runtime, visit(ctx.expression()));
  }

  @Override
  public Node<T> visitRawStringExpression(JmesPathParser.RawStringExpressionContext ctx) {
    String quotedString = ctx.RAW_STRING().getText();
    String unquotedString = rawStringEscapeHelper.unescape(quotedString.substring(1, quotedString.length() - 1));
    return new StringNode<T>(runtime, unquotedString);
  }

  @Override
  public Node<T> visitComparisonExpression(JmesPathParser.ComparisonExpressionContext ctx) {
    String operator = ctx.COMPARATOR().getText();
    Node<T> left = visit(ctx.expression(0));
    Node<T> right = visit(ctx.expression(1));
    return new ComparisonNode<T>(runtime, operator, left, right);
  }

  @Override
  public Node<T> visitParenExpression(JmesPathParser.ParenExpressionContext ctx) {
    return visit(ctx.expression());
  }

  @Override
  public Node<T> visitBracketExpression(JmesPathParser.BracketExpressionContext ctx) {
    return visit(ctx.bracketSpecifier());
  }

  @Override
  public Node<T> visitOrExpression(JmesPathParser.OrExpressionContext ctx) {
    Node<T> left = visit(ctx.expression(0));
    Node<T> right = visit(ctx.expression(1));
    return new OrNode<T>(runtime, left, right);

  }

  @Override
  public Node<T> visitChainExpression(JmesPathParser.ChainExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    Node<T> result = visit(ctx.chainedExpression());
    currentSource.pop();
    return result;
  }

  @Override
  public Node<T> visitAndExpression(JmesPathParser.AndExpressionContext ctx) {
    Node<T> left = visit(ctx.expression(0));
    Node<T> right = visit(ctx.expression(1));
    return new AndNode<T>(runtime, left, right);
  }

  @Override
  public Node<T> visitWildcardExpression(JmesPathParser.WildcardExpressionContext ctx) {
    return visit(ctx.wildcard());
  }

  @Override
  public Node<T> visitBracketedExpression(JmesPathParser.BracketedExpressionContext ctx) {
    currentSource.push(visit(ctx.expression()));
    Node<T> result = visit(ctx.bracketSpecifier());
    currentSource.pop();
    return result;
  }

  @Override
  public Node<T> visitWildcard(JmesPathParser.WildcardContext ctx) {
    return new ForkNode<T>(runtime, new FlattenObjectNode<T>(runtime, currentSource.peek()));
  }

  @Override
  public Node<T> visitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    int n = ctx.expression().size();
    List<Expression<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      entries.add(visit(ctx.expression(i)));
    }
    currentSource.pop();
    return new CreateArrayNode<T>(runtime, entries, currentSource.peek());
  }

  @Override
  public Node<T> visitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    int n = ctx.keyvalExpr().size();
    List<CreateObjectNode.Entry<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      JmesPathParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
      String key = identifierToString(kvCtx.identifier());
      Node<T> value = visit(kvCtx.expression());
      entries.add(new CreateObjectNode.Entry<T>(key, value));
    }
    currentSource.pop();
    return new CreateObjectNode<T>(runtime, entries, currentSource.peek());
  }

  @Override
  public Node<T> visitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    return new IndexNode<T>(runtime, index, currentSource.peek());
  }

  @Override
  public Node<T> visitBracketStar(JmesPathParser.BracketStarContext ctx) {
    return new ForkNode<T>(runtime, currentSource.peek());
  }

  @Override
  public Node<T> visitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
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
  public Node<T> visitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    return new ForkNode<T>(runtime, new FlattenArrayNode<T>(runtime, currentSource.peek()));
  }

  @Override
  public Node<T> visitSelect(JmesPathParser.SelectContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    Node<T> test = visit(ctx.expression());
    currentSource.pop();
    return new ForkNode<T>(runtime, new SelectionNode<T>(runtime, test, currentSource.peek()));
  }

  @Override
  public Node<T> visitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    currentSource.push(new CurrentNode<T>(runtime));
    String name = ctx.NAME().getText();
    int n = ctx.functionArg().size();
    List<Expression<T>> args = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      args.add(visit(ctx.functionArg(i)));
    }
    currentSource.pop();
    Function implementation = runtime.getFunction(name);
    if (implementation == null) {
      Token token = ctx.NAME().getSymbol();
      errors.parseError(String.format("unknown function \"%s\"", name), token.getLine(), token.getStartIndex());
    }
    return new FunctionCallNode<T>(runtime, implementation, args, currentSource.peek());
  }

  @Override
  public Node<T> visitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    if (currentSource.peek() instanceof CurrentNode) {
      return currentSource.peek();
    } else {
      return new CurrentNode<T>(runtime, currentSource.peek());
    }
  }

  @Override
  public Node<T> visitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    return new ExpressionReferenceNode<T>(runtime, visit(ctx.expression()));
  }

  @Override
  public Node<T> visitLiteral(JmesPathParser.LiteralContext ctx) {
    visit(ctx.jsonValue());
    String string = ctx.jsonValue().getText();
    return new JsonLiteralNode<T>(runtime, string, runtime.parseString(string));
  }

  @Override
  public Node<T> visitJsonStringValue(JmesPathParser.JsonStringValueContext ctx) {
    checkForUnescapedBackticks(ctx.getStart());
    return super.visitJsonStringValue(ctx);
  }

  @Override
  public Node<T> visitJsonObjectPair(JmesPathParser.JsonObjectPairContext ctx) {
    checkForUnescapedBackticks(ctx.STRING().getSymbol());
    return super.visitJsonObjectPair(ctx);
  }

  @Override
  public Node<T> visitIdentifier(JmesPathParser.IdentifierContext ctx) {
    return new PropertyNode<T>(runtime, identifierToString(ctx), currentSource.peek());
  }
}
