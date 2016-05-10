package io.burt.jmespath;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import io.burt.jmespath.JmesPathLexer;
import io.burt.jmespath.JmesPathParser;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 0 || args[0].length() == 0) {
      System.exit(1);
    }
    ParseErrorAccumulator errors = new ParseErrorAccumulator();
    JmesPathParser parser = createParser(createLexer(createInput(args[0]), errors), errors);
    ParseTree tree = parser.query();
    if (errors.isEmpty()) {
      System.out.println(tree.toStringTree(parser));
    } else {
      for (ParseError error : errors) {
        System.err.printf("%s at line %d:%d\n", error.message(), error.line(), error.position());
      }
      System.exit(1);
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
