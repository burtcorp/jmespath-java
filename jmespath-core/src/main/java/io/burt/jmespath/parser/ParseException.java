package io.burt.jmespath.parser;

import java.util.Iterator;

import io.burt.jmespath.JmesPathException;

public class ParseException extends JmesPathException implements Iterable<ParseError> {
  private final Iterable<ParseError> errors;

  public ParseException(String query, Iterable<ParseError> errors) {
    super(String.format("Error while parsing \"%s\": %s", query, joinMessages(errors)));
    this.errors = errors;
  }

  private static String joinMessages(Iterable<ParseError> errors) {
    StringBuilder s = new StringBuilder();
    for (ParseError e : errors) {
      s.append(String.format(", %s at position %d", e.message(), e.position()));
    }
    s.delete(0, 2);
    return s.toString();
  }

  public Iterator<ParseError> iterator() { return errors.iterator(); }
}
