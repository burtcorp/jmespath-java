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

  public JmesPathFunction() {
    Function metadata = findFunctionMetadata();
    this.name = findFunctionName(metadata);
    this.minArity = findMinArity(metadata);
    this.maxArity = findMaxArity(metadata);
  }

  private Function findFunctionMetadata() {
    Function metadata = (Function) getClass().getAnnotation(Function.class);
    if (metadata == null) {
      throw new FunctionConfigurationException(String.format("The class %s does not have a @Function annotation", getClass().getName()));
    }
    return metadata;
  }

  private String findFunctionName(Function metadata) {
    if (metadata.name().length() > 0) {
      return metadata.name();
    } else {
      return classNameToFunctionName();
    }
  }

  private int findMaxArity(Function metadata) {
    if (metadata.maxArity() > -1) {
      return metadata.maxArity();
    } else if (metadata.arity() > -1) {
      return metadata.arity();
    } else {
      throw new FunctionConfigurationException(String.format("The class %s's @Function annotation must specify either maxArity or arity", getClass().getName()));
    }
  }

  private int findMinArity(Function metadata) {
    if (metadata.minArity() > -1) {
      return metadata.minArity();
    } else if (metadata.arity() > -1) {
      return metadata.arity();
    } else {
      throw new FunctionConfigurationException(String.format("The class %s's @Function annotation must specify either minArity or arity", getClass().getName()));
    }
  }

  private String classNameToFunctionName() {
    String n = getClass().getName();
    if (n.indexOf("$") > -1) {
      n = n.substring(n.lastIndexOf("$") + 1);
    } else {
      n = n.substring(n.lastIndexOf(".") + 1);
    }
    if (!n.endsWith("Function")) {
      throw new FunctionConfigurationException(String.format("The function defined by %s must either declare its name using the @Function annotation or the class name must end with \"Function\"", getClass().getName()));
    }
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
