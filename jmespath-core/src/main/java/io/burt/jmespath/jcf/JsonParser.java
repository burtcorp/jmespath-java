package io.burt.jmespath.jcf;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.parser.ParseErrorAccumulator;
import io.burt.jmespath.parser.ParseException;
import io.burt.jmespath.parser.JmesPathBaseVisitor;
import io.burt.jmespath.parser.JmesPathParser;
import io.burt.jmespath.parser.JmesPathLexer;

public class JsonParser extends JmesPathBaseVisitor<Object> {
  private final ParseTree tree;
  private final Adapter<Object> adapter;

  public static Object fromString(String json, Adapter<Object> adapter) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(json), errors), errors);
    ParseTree tree = parser.jsonValue();
    if (errors.isEmpty()) {
      return new JsonParser(tree, adapter).object();
    } else {
      throw new ParseException(json, errors);
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

  private JsonParser(ParseTree tree, Adapter<Object> adapter) {
    this.tree = tree;
    this.adapter = adapter;
  }

  public Object object() {
    return visit(tree);
  }

  private String unquote(String quotedString) {
    return quotedString.substring(1, quotedString.length() - 1);
  }

  @Override
  public Object visitJsonObject(JmesPathParser.JsonObjectContext ctx) {
    Map<Object, Object> object = new LinkedHashMap<>(ctx.jsonObjectPair().size());
    for (final JmesPathParser.JsonObjectPairContext pair : ctx.jsonObjectPair()) {
      String key = unquote(pair.STRING().getText());
      Object value = visit(pair.jsonValue());
      object.put(key, value);
    }
    return adapter.createObject(object);
  }

  @Override
  public Object visitJsonArray(JmesPathParser.JsonArrayContext ctx) {
    List<Object> array = new ArrayList<Object>(ctx.jsonValue().size());
    for (final JmesPathParser.JsonValueContext entry : ctx.jsonValue()) {
      array.add(visit(entry));
    }
    return adapter.createArray(array);
  }

  @Override
  public Object visitJsonStringValue(JmesPathParser.JsonStringValueContext ctx) {
    return adapter.createString(unquote(ctx.getText()));
  }

  @Override
  public Object visitJsonNumberValue(JmesPathParser.JsonNumberValueContext ctx) {
    return adapter.createNumber(Double.parseDouble(ctx.getText()));
  }

  @Override
  public Object visitJsonObjectValue(JmesPathParser.JsonObjectValueContext ctx) {
    return visit(ctx.jsonObject());
  }

  @Override
  public Object visitJsonArrayValue(JmesPathParser.JsonArrayValueContext ctx) {
    return visit(ctx.jsonArray());
  }

  @Override
  public Object visitJsonConstantValue(JmesPathParser.JsonConstantValueContext ctx) {
    if (ctx.t != null) {
      return adapter.createBoolean(true);
    } else if (ctx.f != null) {
      return adapter.createBoolean(false);
    } else {
      return adapter.createNull();
    }
  }
}
