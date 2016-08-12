package io.burt.jmespath.util;

import java.util.Arrays;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;

public class StringEscapesTest {
  private static final char[] NO_REPLACEMENTS = new char[0];

  @Test
  public void nothingIsUnescapedByDefault() {
    String unescaped = StringEscapes.unescape(NO_REPLACEMENTS, "hello\\u0020world\\n", false);
    assertThat(unescaped, is("hello\\u0020world\\n"));
  }

  @Test
  public void unicodeEscapesCanBeUnescaped() {
    String unescaped = StringEscapes.unescape(NO_REPLACEMENTS, "hello\\u0020world\\u000a", true);
    assertThat(unescaped, is("hello world\n"));
  }

  @Test
  public void escapesAreReplacedByTheirReplacements() {
    char[] replacements = StringEscapes.createReplacements(
      'n', '\n',
      't', '\t',
      'x', '!'
    );
    String unescaped = StringEscapes.unescape(replacements, "\\thello\\nworld\\x", true);
    assertThat(unescaped, is("\thello\nworld!"));
  }

  @Test
  public void replacementsMustBePairs() {
    try {
      StringEscapes.createReplacements('x', 'x', 'y');
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException iae) {
      assertThat(iae.getMessage(), is("Replacements must be even pairs"));
    }
  }
}
