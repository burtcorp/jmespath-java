package io.burt.jmespath.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;
import java.nio.charset.Charset;

import io.burt.jmespath.Query;

public class AstGenerator {
  public static Query fromString(String query) {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(query), errors), errors);
    ParseTree tree = parser.query();
    if (errors.isEmpty()) {
      AstGeneratingVisitor visitor = new AstGeneratingVisitor(tree);
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
}
