package io.burt.jmespath.function;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;

public class TranslateFunction extends SubstringMatchingFunction {
  public TranslateFunction() {
    super(ArgumentConstraints.listOf(3, 3, ArgumentConstraints.typeOf(JmesPathType.STRING)));
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    String arg = runtime.toString(arguments.get(0).value());
    String map = runtime.toString(arguments.get(1).value());
    String trans = runtime.toString(arguments.get(2).value());

    if (isEmpty(arg)) {
      return runtime.createString("");
    } else {
      return runtime.createString(replaceChars(arg, map, trans));
    }
  }

  /**
   * This is map-based reimplementation of org.apache.commons.lang.StringUtils.replaceChars
   */
  protected static String replaceChars(String input, String from, String to) {
    StringBuffer sb = new StringBuffer();
    Map<Character, Character> map = buildTranslationMap(from, to);

    for (int i = 0; i < input.length(); ++i) {
      Character ch = input.charAt(i);
      if (map.containsKey(ch)) {
        Character tr = map.get(ch);
        if (null != tr) {
          sb.append(tr);
        }
      } else {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

  private static Map<Character, Character> buildTranslationMap(String from, String to) {
    Map<Character, Character> map = new HashMap();
    for (int i = 0; i < from.length(); ++i) {
      map.put(from.charAt(i), i < to.length() ? to.charAt(i) : null);
    }
    return map;
  }
}
