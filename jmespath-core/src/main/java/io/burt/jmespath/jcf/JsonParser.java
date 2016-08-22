package io.burt.jmespath.jcf;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.util.StringEscapeHelper;
import io.burt.jmespath.util.AntlrHelper;
import io.burt.jmespath.parser.ParseErrorAccumulator;
import io.burt.jmespath.parser.ParseException;
import io.burt.jmespath.parser.JmesPathBaseVisitor;
import io.burt.jmespath.parser.JmesPathParser;

public class JsonParser extends JmesPathBaseVisitor<Object> {
  private static final StringEscapeHelper jsonEscapeHelper = new StringEscapeHelper(
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

  private final ParseTree tree;
  private final Adapter<Object> runtime;

  public static Object fromString(String json, Adapter<Object> runtime) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = AntlrHelper.createParser(json, errors);
    ParseTree tree = parser.jsonValue();
    if (errors.isEmpty()) {
      return new JsonParser(tree, runtime).object();
    } else {
      throw new ParseException(json, errors);
    }
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

  @Override
  public Object visitJsonObject(JmesPathParser.JsonObjectContext ctx) {
    Map<Object, Object> object = new LinkedHashMap<>(ctx.jsonObjectPair().size());
    for (final JmesPathParser.JsonObjectPairContext pair : ctx.jsonObjectPair()) {
      String key = jsonEscapeHelper.unescape(unquote(pair.STRING().getText()));
      Object value = visit(pair.jsonValue());
      object.put(key, value);
    }
    return runtime.createObject(object);
  }

  @Override
  public Object visitJsonArray(JmesPathParser.JsonArrayContext ctx) {
    List<Object> array = new ArrayList<>(ctx.jsonValue().size());
    for (final JmesPathParser.JsonValueContext entry : ctx.jsonValue()) {
      array.add(visit(entry));
    }
    return runtime.createArray(array);
  }

  @Override
  public Object visitJsonStringValue(JmesPathParser.JsonStringValueContext ctx) {
    return runtime.createString(jsonEscapeHelper.unescape(unquote(ctx.getText())));
  }

  @Override
  public Object visitJsonNumberValue(JmesPathParser.JsonNumberValueContext ctx) {
    if (ctx.REAL_OR_EXPONENT_NUMBER() != null) {
      return runtime.createNumber(Double.parseDouble(ctx.getText()));
    } else {
      return runtime.createNumber(Long.parseLong(ctx.getText()));
    }
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
    switch (ctx.getText().charAt(0)) {
      case 't': return runtime.createBoolean(true);
      case 'f': return runtime.createBoolean(false);
      case 'n': return runtime.createNull();
      default: throw new IllegalStateException(String.format("Expected true, false or null but encountered %s", ctx.getText()));
    }
  }
}
