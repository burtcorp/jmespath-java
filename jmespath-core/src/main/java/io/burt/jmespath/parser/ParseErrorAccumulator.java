package io.burt.jmespath.parser;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.RecognitionException;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

public class ParseErrorAccumulator extends BaseErrorListener implements Iterable<ParseError> {
  private final List<ParseError> errors;

  public ParseErrorAccumulator() {
    this.errors = new LinkedList<>();
  }

  @Override
  public Iterator<ParseError> iterator() {
    return errors.iterator();
  }

  public boolean isEmpty() {
    return errors.isEmpty();
  }

  @Override
  public void syntaxError(Recognizer<?,?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    errors.add(new ParseError(String.format("syntax error %s", msg), charPositionInLine));
  }

  public void parseError(String msg, int charPositionInLine) {
    errors.add(new ParseError(msg, charPositionInLine));
  }
}
