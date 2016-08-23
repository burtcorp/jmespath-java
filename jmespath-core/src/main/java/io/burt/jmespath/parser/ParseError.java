package io.burt.jmespath.parser;

public class ParseError {
  private final String message;
  private final int position;

  public ParseError(String message, int position) {
    this.message = message;
    this.position = position;
  }

  public String message() { return message; }

  public int position() { return position; }
}
