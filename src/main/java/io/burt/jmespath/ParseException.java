package io.burt.jmespath;

import java.util.Iterator;

public class ParseException extends JmesPathException implements Iterable<ParseError> {
  private final Iterable<ParseError> errors;

  public ParseException(Iterable<ParseError> errors) {
    super("There were errors");
    this.errors = errors;
  }

  public Iterator<ParseError> iterator() { return errors.iterator(); }
}
