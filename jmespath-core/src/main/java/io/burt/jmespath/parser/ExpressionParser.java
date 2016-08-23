package io.burt.jmespath.parser;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

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

  private final ParseTree tree;
  private final Adapter<T> runtime;
  private final NodeFactory<T> nodeFactory;
  private final ParseErrorAccumulator errors;

  private Node<T> chainedNode;

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
      errors.parseError("syntax error unexpected `", token.getStartIndex() + unescapedBacktickIndex);
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

  private Node<T> createProjectionIfChained(Node<T> node) {
    if (chainedNode != null) {
      node = nodeFactory.createSequence(Arrays.asList(node, nodeFactory.createProjection(chainedNode)));
      chainedNode = null;
    }
    return node;
  }

  private Node<T> createSequenceIfChained(Node<T> node) {
    if (chainedNode != null) {
      node = nodeFactory.createSequence(Arrays.asList(node, chainedNode));
      chainedNode = null;
    }
    return node;
  }

  private Node<T> nonChainingVisit(ParseTree tree) {
    Node<T> stashedNextNode = chainedNode;
    chainedNode = null;
    Node<T> result = createSequenceIfChained(visit(tree));
    chainedNode = stashedNextNode;
    return result;
  }

  @Override
  public Node<T> visitJmesPathExpression(JmesPathParser.JmesPathExpressionContext ctx) {
    return createSequenceIfChained(visit(ctx.expression()));
  }

  @Override
  public Node<T> visitPipeExpression(JmesPathParser.PipeExpressionContext ctx) {
    Node<T> right = createSequenceIfChained(visit(ctx.expression(1)));
    Node<T> left = createSequenceIfChained(visit(ctx.expression(0)));
    return nodeFactory.createSequence(Arrays.asList(left, right));
  }

  @Override
  public Node<T> visitIdentifierExpression(JmesPathParser.IdentifierExpressionContext ctx) {
    return visit(ctx.identifier());
  }

  @Override
  public Node<T> visitNotExpression(JmesPathParser.NotExpressionContext ctx) {
    return nodeFactory.createNegate(createSequenceIfChained(visit(ctx.expression())));
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
    Node<T> right = nonChainingVisit(ctx.expression(1));
    Node<T> left = nonChainingVisit(ctx.expression(0));
    return createSequenceIfChained(nodeFactory.createComparison(operator, left, right));
  }

  @Override
  public Node<T> visitParenExpression(JmesPathParser.ParenExpressionContext ctx) {
    return createSequenceIfChained(nonChainingVisit(ctx.expression()));
  }

  @Override
  public Node<T> visitBracketExpression(JmesPathParser.BracketExpressionContext ctx) {
    Node<T> result = visit(ctx.bracketSpecifier());
    if (result == null) {
      result = chainedNode;
      chainedNode = null;
    }
    return result;
  }

  @Override
  public Node<T> visitOrExpression(JmesPathParser.OrExpressionContext ctx) {
    Node<T> left = nonChainingVisit(ctx.expression(0));
    Node<T> right = nonChainingVisit(ctx.expression(1));
    return createSequenceIfChained(nodeFactory.createOr(left, right));
  }

  @Override
  public Node<T> visitChainExpression(JmesPathParser.ChainExpressionContext ctx) {
    chainedNode = visit(ctx.chainedExpression());
    return createSequenceIfChained(visit(ctx.expression()));
  }

  @Override
  public Node<T> visitAndExpression(JmesPathParser.AndExpressionContext ctx) {
    Node<T> left = nonChainingVisit(ctx.expression(0));
    Node<T> right = nonChainingVisit(ctx.expression(1));
    return createSequenceIfChained(nodeFactory.createAnd(left, right));
  }

  @Override
  public Node<T> visitWildcardExpression(JmesPathParser.WildcardExpressionContext ctx) {
    return visit(ctx.wildcard());
  }

  @Override
  public Node<T> visitBracketedExpression(JmesPathParser.BracketedExpressionContext ctx) {
    Node<T> chainAfterExpression = visit(ctx.bracketSpecifier());
    Node<T> expression = createSequenceIfChained(visit(ctx.expression()));
    chainedNode = chainAfterExpression;
    return createSequenceIfChained(expression);
  }

  @Override
  public Node<T> visitWildcard(JmesPathParser.WildcardContext ctx) {
    return createProjectionIfChained(nodeFactory.createFlattenObject());
  }

  @Override
  public Node<T> visitMultiSelectList(JmesPathParser.MultiSelectListContext ctx) {
    int n = ctx.expression().size();
    List<Expression<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      entries.add(nonChainingVisit(ctx.expression(i)));
    }
    return createSequenceIfChained(nodeFactory.createCreateArray(entries));
  }

  @Override
  public Node<T> visitMultiSelectHash(JmesPathParser.MultiSelectHashContext ctx) {
    int n = ctx.keyvalExpr().size();
    List<Entry<T>> entries = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      JmesPathParser.KeyvalExprContext kvCtx = ctx.keyvalExpr(i);
      String key = identifierToString(kvCtx.identifier());
      Node<T> value = nonChainingVisit(kvCtx.expression());
      entries.add(new Entry<>(key, value));
    }
    return createSequenceIfChained(nodeFactory.createCreateObject(entries));
  }

  @Override
  public Node<T> visitBracketIndex(JmesPathParser.BracketIndexContext ctx) {
    int index = Integer.parseInt(ctx.SIGNED_INT().getText());
    chainedNode = createSequenceIfChained(nodeFactory.createIndex(index));
    return null;
  }

  @Override
  public Node<T> visitBracketStar(JmesPathParser.BracketStarContext ctx) {
    Node<T> projection = (chainedNode == null) ? nodeFactory.createCurrent() : chainedNode;
    chainedNode = nodeFactory.createProjection(projection);
    return null;
  }

  @Override
  public Node<T> visitBracketSlice(JmesPathParser.BracketSliceContext ctx) {
    Integer start = null;
    Integer stop = null;
    Integer step = null;
    JmesPathParser.SliceContext sliceCtx = ctx.slice();
    if (sliceCtx.start != null) {
      start = Integer.parseInt(sliceCtx.start.getText());
    }
    if (sliceCtx.stop != null) {
      stop = Integer.parseInt(sliceCtx.stop.getText());
    }
    if (sliceCtx.step != null) {
      step = Integer.parseInt(sliceCtx.step.getText());
      if (step == 0) {
        errors.parseError(String.format("invalid value %d for step size", step), sliceCtx.step.getStartIndex());
      }
    }
    chainedNode = createProjectionIfChained(nodeFactory.createSlice(start, stop, step));
    return null;
  }

  @Override
  public Node<T> visitBracketFlatten(JmesPathParser.BracketFlattenContext ctx) {
    return createProjectionIfChained(nodeFactory.createFlattenArray());
  }

  @Override
  public Node<T> visitSelect(JmesPathParser.SelectContext ctx) {
    chainedNode = createProjectionIfChained(nodeFactory.createSelection(nonChainingVisit(ctx.expression())));
    return null;
  }

  @Override
  public Node<T> visitFunctionExpression(JmesPathParser.FunctionExpressionContext ctx) {
    String name = ctx.NAME().getText();
    int n = ctx.functionArg().size();
    List<Expression<T>> args = new ArrayList<>(n);
    for (int i = 0; i < n; i++) {
      args.add(nonChainingVisit(ctx.functionArg(i)));
    }
    Function implementation = runtime.functionRegistry().getFunction(name);
    if (implementation == null) {
      Token token = ctx.NAME().getSymbol();
      errors.parseError(String.format("unknown function \"%s\"", name), token.getStartIndex());
    }
    return createSequenceIfChained(nodeFactory.createFunctionCall(implementation, args));
  }

  @Override
  public Node<T> visitCurrentNode(JmesPathParser.CurrentNodeContext ctx) {
    if (chainedNode == null) {
      return nodeFactory.createCurrent();
    } else {
      Node<T> result = chainedNode;
      chainedNode = null;
      return result;
    }
  }

  @Override
  public Node<T> visitExpressionType(JmesPathParser.ExpressionTypeContext ctx) {
    Node<T> expression = createSequenceIfChained(visit(ctx.expression()));
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
    return createSequenceIfChained(nodeFactory.createProperty(identifierToString(ctx)));
  }
}
