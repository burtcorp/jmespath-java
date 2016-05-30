package io.burt.jmespath.jp;

import io.burt.jmespath.Query;
import io.burt.jmespath.JmesPathException;

public class Main {
  public static void main(String[] args) {
    if (args.length == 0 || args[0].length() == 0) {
      System.exit(1);
    }
    try {
      Query query = Query.fromString(args[0]);
      System.err.println(query);
    } catch (JmesPathException jpe) {
      System.err.println(jpe.getMessage());
      System.exit(1);
    }
  }
}
