package io.burt.jmespath.util;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;

import io.burt.jmespath.parser.JmesPathLexer;
import io.burt.jmespath.parser.JmesPathParser;

public class AntlrHelper {
  private AntlrHelper() { }

  private static JmesPathLexer createLexer(String input, ANTLRErrorListener errorListener) {
    JmesPathLexer lexer = new JmesPathLexer(new ANTLRInputStream(input));
    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);
    lexer.addErrorListener(errorListener);
    return lexer;
  }

  public static JmesPathParser createParser(String input, ANTLRErrorListener errorListener) {
    JmesPathLexer lexer = createLexer(input, errorListener);
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    JmesPathParser parser = new JmesPathParser(tokens);
    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
    parser.addErrorListener(errorListener);
    parser.setBuildParseTree(true);
    return parser;
  }
}