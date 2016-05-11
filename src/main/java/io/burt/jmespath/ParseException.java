package io.burt.jmespath;

import java.util.Iterator;

public class ParseException extends JmesPathException implements Iterable<ParseError> {
  private final Iterable<ParseError> errors;

  public ParseException(String query, Iterable<ParseError> errors) {
    super(String.format("Error while parsing \"%s\": %s", query, joinMessages(errors)));
    this.errors = errors;
  }

  private static String joinMessages(Iterable<ParseError> errors) {
    StringBuffer s = new StringBuffer();
    for (ParseError e : errors) {
      s.append(String.format(", %s at position %d", e.message(), e.position()));
    }
    s.delete(0, 2);
    return s.toString();
  }

  public Iterator<ParseError> iterator() { return errors.iterator(); }
}
