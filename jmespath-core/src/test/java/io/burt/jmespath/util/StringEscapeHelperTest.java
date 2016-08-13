package io.burt.jmespath.util;

import java.util.Arrays;

import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;

public class StringEscapeHelperTest {
  @Test
  public void nothingIsUnescapedByDefault() {
    StringEscapeHelper escapeHelper = new StringEscapeHelper(false);
    String unescaped = escapeHelper.unescape("hello\\u0020world\\n");
    assertThat(unescaped, is("hello\\u0020world\\n"));
  }

  @Test
  public void unicodeEscapesCanBeUnescaped() {
    StringEscapeHelper escapeHelper = new StringEscapeHelper(true);
    String unescaped = escapeHelper.unescape("hello\\u0020world\\u000a");
    assertThat(unescaped, is("hello world\n"));
  }

  @Test
  public void escapesAreReplacedByTheirReplacements() {
    StringEscapeHelper escapeHelper = new StringEscapeHelper(
      'n', '\n',
      't', '\t',
      'x', '!'
    );
    String unescaped = escapeHelper.unescape("\\thello\\nworld\\x");
    assertThat(unescaped, is("\thello\nworld!"));
  }

  @Test
  public void replacementsMustBePairs() {
    try {
      new StringEscapeHelper('x', 'x', 'y');
      fail("Expected IllegalArgumentException to be thrown");
    } catch (IllegalArgumentException iae) {
      assertThat(iae.getMessage(), is("Replacements must be even pairs"));
    }
  }
}
