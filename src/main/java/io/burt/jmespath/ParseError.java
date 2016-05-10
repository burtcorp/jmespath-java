package io.burt.jmespath;

public class ParseError {
  private final String message;
  private final int line;
  private final int position;

  public ParseError(String message, int line, int position) {
    this.message = message;
    this.line = line;
    this.position = position;
  }

  public String message() { return message; }

  public int line() { return line; }

  public int position() { return position; }
}
