package io.burt.jmespath.util;

import java.util.Arrays;

public class StringEscapes {
  public static final char NO_REPLACEMENT = 0xffff;

  public static char[] createReplacements(char... pairs) {
    if (pairs.length % 2 == 1) {
      throw new IllegalArgumentException("Replacements must be even pairs");
    }
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

  public static String unescape(char[] replacements, String str, boolean unescapeUnicodeEscapes) {
    int slashIndex = str.indexOf('\\');
    if (slashIndex > -1) {
      int offset = 0;
      StringBuilder unescaped = new StringBuilder();
      while (slashIndex > -1) {
        char c = str.charAt(slashIndex + 1);
        char r = (c < replacements.length) ? replacements[c] : NO_REPLACEMENT;
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
}
