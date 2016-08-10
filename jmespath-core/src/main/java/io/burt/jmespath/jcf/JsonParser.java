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
  private final Adapter<Object> runtime;

  public static Object fromString(String json, Adapter<Object> runtime) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(json), errors), errors);
    ParseTree tree = parser.jsonValue();
    if (errors.isEmpty()) {
      return new JsonParser(tree, runtime).object();
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

  private JsonParser(ParseTree tree, Adapter<Object> runtime) {
    this.tree = tree;
    this.runtime = runtime;
  }

  public Object object() {
    return visit(tree);
  }

  private String unquote(String quotedString) {
    return quotedString.substring(1, quotedString.length() - 1);
  }

  private String unescape(String str) {
    int slashIndex = str.indexOf("\\");
    if (slashIndex > -1) {
      int offset = 0;
      StringBuilder builder = new StringBuilder(str);
      while (slashIndex > -1) {
        int length = -1;
        String replacement = null;
        char escapeChar = builder.charAt(slashIndex + 1);
        switch (escapeChar) {
          case '"':
            length = 2;
            replacement = "\"";
            break;
          case '/':
            length = 2;
            replacement = "/";
            break;
          case '\\':
            length = 2;
            replacement = "\\";
            break;
          case 'b':
            length = 2;
            replacement = "\b";
            break;
          case 'f':
            length = 2;
            replacement = "\f";
            break;
          case 'n':
            length = 2;
            replacement = "\n";
            break;
          case 'r':
            length = 2;
            replacement = "\r";
            break;
          case 't':
            length = 2;
            replacement = "\t";
            break;
          case 'u':
            String hexCode = builder.substring(slashIndex + 2, slashIndex + 6);
            length = 6;
            replacement = new String(Character.toChars(Integer.parseInt(hexCode, 16)));
            break;
          default:
            throw new IllegalStateException(String.format("Bad escape encountered \"\\%s\" (in \"%s\")", escapeChar, tree.getText()));
        }
        builder.replace(slashIndex, slashIndex + length, replacement);
        offset = slashIndex + 1;
        slashIndex = builder.indexOf("\\", offset);
      }
      return builder.toString();
    } else {
      return str;
    }
  }

  @Override
  public Object visitJsonObject(JmesPathParser.JsonObjectContext ctx) {
    Map<Object, Object> object = new LinkedHashMap<>(ctx.jsonObjectPair().size());
    for (final JmesPathParser.JsonObjectPairContext pair : ctx.jsonObjectPair()) {
      String key = unescape(unquote(pair.STRING().getText()));
      Object value = visit(pair.jsonValue());
      object.put(key, value);
    }
    return runtime.createObject(object);
  }

  @Override
  public Object visitJsonArray(JmesPathParser.JsonArrayContext ctx) {
    List<Object> array = new ArrayList<Object>(ctx.jsonValue().size());
    for (final JmesPathParser.JsonValueContext entry : ctx.jsonValue()) {
      array.add(visit(entry));
    }
    return runtime.createArray(array);
  }

  @Override
  public Object visitJsonStringValue(JmesPathParser.JsonStringValueContext ctx) {
    return runtime.createString(unescape(unquote(ctx.getText())));
  }

  @Override
  public Object visitJsonNumberValue(JmesPathParser.JsonNumberValueContext ctx) {
    return runtime.createNumber(Double.parseDouble(ctx.getText()));
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
      return runtime.createBoolean(true);
    } else if (ctx.f != null) {
      return runtime.createBoolean(false);
    } else {
      return runtime.createNull();
    }
  }
}
