package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import io.burt.jmespath.Adapter;

public abstract class JmesPathFunction {
  private final ArgumentConstraint argumentConstraints;
  private final String name;

  private static Pattern CAMEL_CASE_COMPONENT_RE = Pattern.compile("[A-Z][^A-Z]+");

  public JmesPathFunction(ArgumentConstraint argumentConstraints) {
    this(null, argumentConstraints);
  }

  public JmesPathFunction(ArgumentConstraint... argumentConstraints) {
    this(null, ArgumentConstraints.listOf(argumentConstraints));
  }

  public JmesPathFunction(String name, ArgumentConstraint... argumentConstraints) {
    this(name, ArgumentConstraints.listOf(argumentConstraints));
  }

  public JmesPathFunction(String name, ArgumentConstraint argumentConstraints) {
    this.name = name == null ? classNameToFunctionName() : name;
    this.argumentConstraints = argumentConstraints;
  }

  private String classNameToFunctionName() {
    String n = getClass().getName();
    if (n.indexOf("$") > -1) {
      n = n.substring(n.lastIndexOf("$") + 1);
    } else {
      n = n.substring(n.lastIndexOf(".") + 1);
    }
    if (!n.endsWith("Function")) {
      throw new FunctionConfigurationException(String.format("The function defined by %s must either pass a name to the JmesPathFunction constructor or the class name must end with \"Function\"", getClass().getName()));
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

  protected ArgumentConstraint argumentConstraints() {
    return argumentConstraints;
  }

  public <T> T call(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    int totalArguments = arguments.size();
    try {
      Iterator<ExpressionOrValue<T>> argumentIterator = arguments.iterator();
      argumentConstraints.check(adapter, argumentIterator);
      if (argumentIterator.hasNext()) {
        throw new ArityException(name(), argumentConstraints.minArity(), argumentConstraints.maxArity(), totalArguments);
      } else {
        return internalCall(adapter, arguments);
      }
    } catch (ArgumentConstraints.InternalArityException e) {
      throw new ArityException(name(), argumentConstraints.minArity(), argumentConstraints.maxArity(), totalArguments);
    } catch (ArgumentConstraints.InternalArgumentTypeException e) {
      throw new ArgumentTypeException(name(), e.expectedType(), e.actualType());
    }
  }

  protected abstract <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments);
}
