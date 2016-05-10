package io.burt.jmespath;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import io.burt.jmespath.JmesPathLexer;
import io.burt.jmespath.JmesPathParser;
import io.burt.jmespath.Query;

public class AstGenerator {
  public static Query fromString(String query) throws IOException {
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(query), errors), errors);
    ParseTree tree = parser.query();
    if (errors.isEmpty()) {
      ParseTreeWalker walker = new ParseTreeWalker();
      AstGeneratingListener listener = new AstGeneratingListener();
      walker.walk(listener, tree);
      return listener.ast();
    } else {
      throw new ParseException(errors);
    }
  }

  private static ANTLRInputStream createInput(String query) throws IOException {
    InputStream is = new ByteArrayInputStream(query.getBytes(Charset.forName("UTF-8")));
    return new ANTLRInputStream(is);
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
