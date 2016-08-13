package io.burt.jmespath.util;

import java.util.Arrays;

public class StringEscapeHelper {
  private static final char NO_REPLACEMENT = 0xffff;

  private final char[] unescapeMap;
  private final char[] escapeMap;
  private final boolean unescapeUnicodeEscapes;

  public StringEscapeHelper(char... replacementPairs) {
    this(false, replacementPairs);
  }

  public StringEscapeHelper(boolean unescapeUnicodeEscapes, char... replacementPairs) {
    if (replacementPairs.length % 2 == 1) {
      throw new IllegalArgumentException("Replacements must be even pairs");
    }
    this.unescapeUnicodeEscapes = unescapeUnicodeEscapes;
    this.unescapeMap = createUnescapeMap(replacementPairs);
    this.escapeMap = createEscapeMap(replacementPairs);
  }

  private static char[] createUnescapeMap(char[] pairs) {
    char max = 0;
    int i;
    for (i = 0; i < pairs.length; i += 2) {
      if (pairs[i] > max) {
        max = pairs[i];
      }
    }
    char[] replacementsMap = new char[max + 1];
    Arrays.fill(replacementsMap, NO_REPLACEMENT);
    for (i = 0; i < pairs.length; i += 2) {
      replacementsMap[pairs[i]] = pairs[i + 1];
    }
    return replacementsMap;
  }

  private static char[] createEscapeMap(char[] pairs) {
    char max = 0;
    int i;
    for (i = 0; i < pairs.length; i += 2) {
      if (pairs[i + 1] > max) {
        max = pairs[i + 1];
      }
    }
    char[] replacementsMap = new char[max + 1];
    Arrays.fill(replacementsMap, NO_REPLACEMENT);
    for (i = 0; i < pairs.length; i += 2) {
      replacementsMap[pairs[i + 1]] = pairs[i];
    }
    return replacementsMap;
  }

  public String unescape(String str) {
    int slashIndex = str.indexOf('\\');
    if (slashIndex > -1) {
      int offset = 0;
      StringBuilder unescaped = new StringBuilder();
      while (slashIndex > -1) {
        char c = str.charAt(slashIndex + 1);
        char r = (c < unescapeMap.length) ? unescapeMap[c] : NO_REPLACEMENT;
        if (r != NO_REPLACEMENT) {
          unescaped.append(str.substring(offset, slashIndex));
          unescaped.append(r);
          offset = slashIndex + 2;
        } else if (unescapeUnicodeEscapes && c == 'u') {
          String hexCode = str.substring(slashIndex + 2, slashIndex + 6);
          String replacement = new String(Character.toChars(Integer.parseInt(hexCode, 16)));
          unescaped.append(str.substring(offset, slashIndex));
          unescaped.append(replacement);
          offset = slashIndex + 6;
        }
        slashIndex = str.indexOf('\\', slashIndex + 2);
      }
      unescaped.append(str.substring(offset, str.length()));
      return unescaped.toString();
    } else {
      return str;
    }
  }

  public String escape(String str) {
    StringBuilder escaped = new StringBuilder();
    int offset = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      char r = (c < escapeMap.length) ? escapeMap[c] : NO_REPLACEMENT;
      if (r != NO_REPLACEMENT) {
        escaped.append(str.substring(offset, i));
        escaped.append('\\');
        escaped.append(r);
        offset = i + 1;
      }
    }
    if (offset < str.length()) {
      escaped.append(str.substring(offset, str.length()));
    }
    return escaped.toString();
  }
}
