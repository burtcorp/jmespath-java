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

public class Main {
  public static void main(String[] args) {
    if (args.length == 0 || args[0].length() == 0) {
      System.exit(1);
    }
    try {
      Query query = Query.fromString(args[0]);
      System.err.println(query);
    } catch (ParseException pe) {
      System.err.println(pe.getMessage());
      System.exit(1);
    }
  }
}
