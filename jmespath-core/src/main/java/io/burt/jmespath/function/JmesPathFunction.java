package io.burt.jmespath.function;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import io.burt.jmespath.Adapter;

public abstract class JmesPathFunction {
  private final int minArity;
  private final int maxArity;
  private final String name;

  private static Pattern CAMEL_CASE_COMPONENT_RE = Pattern.compile("[A-Z][^A-Z]+");

  public JmesPathFunction(int minArity, int maxArity) {
    this.minArity = minArity;
    this.maxArity = maxArity;
    this.name = classNameToFunctionName();
  }

  private String classNameToFunctionName() {
    String n = getClass().getName();
    n = n.substring(n.lastIndexOf(".") + 1);
    Matcher m = CAMEL_CASE_COMPONENT_RE.matcher(n);
    int offset = 0;
    StringBuilder snakeCaseName = new StringBuilder();
    while (m.find(offset)) {
      String piece = n.substring(m.start(), m.end()).toLowerCase();
      if (piece.equals("function")) {
        break;
      }
      snakeCaseName.append(piece);
      snakeCaseName.append("_");
      offset = m.end();
    }
    snakeCaseName.deleteCharAt(snakeCaseName.length() - 1);
    return snakeCaseName.toString();
  }

  public String name() {
    return name;
  }

  public <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    int numArguments = arguments.size();
    if (numArguments >= minArity && numArguments <= maxArity) {
      return internalCall(adapter, arguments);
    } else {
      throw new ArityException(name(), minArity, maxArity, numArguments);
    }
  }

  protected abstract <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments);
}
