package io.burt.jmespath.parser;

import java.util.Deque;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Expression;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.util.StringEscapeHelper;
import io.burt.jmespath.util.AntlrHelper;
import io.burt.jmespath.node.NodeFactory;
import io.burt.jmespath.node.Node;
import io.burt.jmespath.node.CreateObjectNode.Entry;
import io.burt.jmespath.node.ProjectionNode;
import io.burt.jmespath.node.CurrentNode;
import io.burt.jmespath.node.Operator;

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

  private static final StringEscapeHelper jsonLiteralEscapeHelper = new StringEscapeHelper(
    false,
    '`', '`'
  );

  private static class StartProjectionNode<T> extends Node<T> {
    public StartProjectionNode(Adapter<T> runtime, Node<T> source) {
      super(runtime, source);
    }

    @Override
    public Node<T> copyWithSource(Node<T> source) {
      return new StartProjectionNode<>(runtime, source);
    }

    @Override
    protected boolean internalEquals(Object o) {
      return true;
    }

    @Override
    protected int internalHashCode() {
      return 31;
    }
  }

  private static class StopProjectionsNode<T> extends Node<T> {
    public StopProjectionsNode(Adapter<T> runtime, Node<T> source) {
      super(runtime, source);
    }

    @Override
    public Node<T> copyWithSource(Node<T> source) {
      return new StopProjectionsNode<>(runtime, source);
    }

    @Override
    protected boolean internalEquals(Object o) {
      return true;
    }

    @Override
    protected int internalHashCode() {
      return 31;
    }
  }

  private final ParseTree tree;
  private final Deque<Node<T>> currentSource;
  private final Adapter<T> runtime;
  private final NodeFactory<T> nodeFactory;
  private final ParseErrorAccumulator errors;

  public static <U> Expression<U> fromString(Adapter<U> runtime, String rawExpression) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = AntlrHelper.createParser(rawExpression, errors);
    ParseTree tree = parser.jmesPathExpression();
    Expression<U> expression = null;
    if (errors.isEmpty()) {
      ExpressionParser<U> visitor = new ExpressionParser<>(runtime, tree, errors);
      expression = visitor.expression();
    }
    if (!errors.isEmpty()) {
      throw new ParseException(rawExpression, errors);
    }
    return expression;
  }

  private ExpressionParser(Adapter<T> runtime, ParseTree tree, ParseErrorAccumulator errors) {
    this.runtime = runtime;
    this.nodeFactory = runtime.nodeFactory();
    this.tree = tree;
    this.errors = errors;
    this.currentSource = new LinkedList<>();
  }

  public Expression<T> expression() {
    return rewriteProjections(visit(tree));
  }

  private Node<T> createCurrent() {
    return nodeFactory.createCurrent();
  }

  private Node<T> createProjection(Expression<T> expression, Node<T> source) {
    return nodeFactory.createProjection(expression, source);
  }

  private Node<T> startProjection(Node<T> source) {
    return new StartProjectionNode<>(runtime, source);
  }

  private Node<T> stopProjections(Node<T> source) {
    return new StopProjectionsNode<>(runtime, source);
  }

  private Node<T> rewriteProjections(Node<T> node) {
    return removeStopProjections(rewriteProjections(node, node));
  }

  private Node<T> rewriteProjections(Node<T> node, Node<T> root) {
    Node<T> source = node.source();
    if (source == null) {
      return node;
    } else if (node instanceof StartProjectionNode) {
      Node<T> rearrangedSource = rewriteProjections(source, source);
      return createProjection(createCurrent(), rearrangedSource);
    } else if (source instanceof StopProjectionsNode) {
      Node<T> rearrangedSource = rewriteProjections(source, source);
      return node.copyWithSource(rearrangedSource);
    } else if (source instanceof StartProjectionNode) {
      Node<T> projectionExpression = removeStopProjections(reSource(root, source, createCurrent()));
      Node<T> projection = createProjection(projectionExpression, source.source());
      return rewriteProjections(projection, projection);
    } else {
      Node<T> rearrangedSource = rewriteProjections(source, root);
      if (rearrangedSource instanceof ProjectionNode) {
        return rearrangedSource;
      } else {
        return node.copyWithSource(rearrangedSource);
      }
    }
  }

  private Node<T> removeStopProjections(Node<T> node) {
    if (node instanceof StopProjectionsNode) {
      return removeStopProjections(node.source());
    } else if (node.source() == null) {
      return node;
    } else {
      return node.copyWithSource(removeStopProjections(node.source()));
    }
  }

  private Node<T> reSource(Node<T> root, Node<T> node, Node<T> replacement) {
    Node<T> newSource = replacement;
    if (root.source() != node) {
      newSource = reSource(root.source(), node, replacement);
    }
    return root.copyWithSource(newSource);
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
    currentSource.push(createCurrent());
    Node<T> result = visit(ctx.expression());
    currentSource.pop();
    return result;
  }

  @Override
  public Node<T> visitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    currentSource.push(stopProjections(visit(ctx.expression(0))));
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
    return nodeFactory.createNegate(visit(ctx.expression()));
  }

  @Override
  public Node<T> visitRawStringExpression(JmesPathParser.RawStringExpressionContext ctx) {
    String quotedString = ctx.RAW_STRING().getText();
    String unquotedString = rawStringEscapeHelper.unescape(quotedString.substring(1, quotedString.length() - 1));
    return nodeFactory.createString(unquotedString);
  }

  @Override
  public Node<T> visitComparisonExpression(JmesPathParser.ComparisonExpressionContext ctx) {
    Operator operator = Operator.fromString(ctx.COMPARATOR().getText());
    Node<T> left = rewriteProjections(visit(ctx.expression(0)));
    Node<T> right = rewriteProjections(visit(ctx.expression(1)));
    return nodeFactory.createComparison(operator, left, right);
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
    Node<T> left = rewriteProjections(visit(ctx.expression(0)));
    Node<T> right = rewriteProjections(visit(ctx.expression(1)));
    return nodeFactory.createOr(left, right);
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
    Node<T> left = rewriteProjections(visit(ctx.expression(0)));
    Node<T> right = rewriteProjections(visit(ctx.expression(1)));
    return nodeFactory.createAnd(left, right);
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
    return startProjection(nodeFactory.createFlattenObject(currentSource.peek()));
  }

  @Override
  public Node<T> visitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    currentSource.push(createCurrent());
    int n = ctx.expression().size();
    List<Expression<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      entries.add(rewriteProjections(visit(ctx.expression(i))));
    }
    currentSource.pop();
    return nodeFactory.createCreateArray(entries, currentSource.peek());
  }

  @Override
  public Node<T> visitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    currentSource.push(createCurrent());
    int n = ctx.keyvalExpr().size();
    List<Entry<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      JmesPathParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
      String key = identifierToString(kvCtx.identifier());
      Node<T> value = rewriteProjections(visit(kvCtx.expression()));
      entries.add(new Entry<>(key, value));
    }
    currentSource.pop();
    return nodeFactory.createCreateObject(entries, currentSource.peek());
  }

  @Override
  public Node<T> visitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    return nodeFactory.createIndex(index, currentSource.peek());
  }

  @Override
  public Node<T> visitBracketStar(JmesPathParser.BracketStarContext ctx) {
    return startProjection(currentSource.peek());
  }

  @Override
  public Node<T> visitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
    Integer start = null;
    Integer stop = null;
    Integer step = 1;
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
    return startProjection(nodeFactory.createSlice(start, stop, step, currentSource.peek()));
  }

  @Override
  public Node<T> visitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    return startProjection(nodeFactory.createFlattenArray(stopProjections(currentSource.peek())));
  }

  @Override
  public Node<T> visitSelect(JmesPathParser.SelectContext ctx) {
    currentSource.push(createCurrent());
    Node<T> test = rewriteProjections(visit(ctx.expression()));
    currentSource.pop();
    return startProjection(nodeFactory.createSelection(test, currentSource.peek()));
  }

  @Override
  public Node<T> visitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    currentSource.push(createCurrent());
    String name = ctx.NAME().getText();
    int n = ctx.functionArg().size();
    List<Expression<T>> args = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      args.add(rewriteProjections(visit(ctx.functionArg(i))));
    }
    currentSource.pop();
    Function implementation = runtime.functionRegistry().getFunction(name);
    if (implementation == null) {
      Token token = ctx.NAME().getSymbol();
      errors.parseError(String.format("unknown function \"%s\"", name), token.getLine(), token.getStartIndex());
    }
    return nodeFactory.createFunctionCall(implementation, args, currentSource.peek());
  }

  @Override
  public Node<T> visitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    if (currentSource.peek() instanceof CurrentNode) {
      return currentSource.peek();
    } else {
      return nodeFactory.createCurrent(currentSource.peek());
    }
  }

  @Override
  public Node<T> visitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    Node<T> expression = rewriteProjections(visit(ctx.expression()));
    return nodeFactory.createExpressionReference(expression);
  }

  @Override
  public Node<T> visitLiteral(JmesPathParser.LiteralContext ctx) {
    visit(ctx.jsonValue());
    String string = jsonLiteralEscapeHelper.unescape(ctx.jsonValue().getText());
    return nodeFactory.createJsonLiteral(string);
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
    return nodeFactory.createProperty(identifierToString(ctx), currentSource.peek());
  }
}
