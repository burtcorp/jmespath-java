package io.burt.jmespath.function;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import io.burt.jmespath.Adapter;

/**
 * Base class of all functions.
 * <p>
 * Subclasses must either use the constructor that specifies a name, or be
 * called something that ends with "Function" – in which case the name will
 * automatically be generated from the class name. "MyAwesomeFunction" will
 * get the name "my_awesome", i.e. the camel cased name will be converted to
 * snake case, minus the "Function" suffix.
 * <p>
 * Subclasses must override the {@link #callFunction} method, and not
 * {@link #call}. The latter does type checking on the arguments and then calls
 * {@link #callFunction}.
 * <p>
 * Subclasses must also provide argument constraints for checking arguments.
 * This is done by using the {@link ArgumentConstraints} DSL and passing the
 * result in a <code>super</code> call in the constructor.
 */
public abstract class BaseFunction implements Function {
  private final ArgumentConstraint argumentConstraints;
  private final String name;

  private static final Pattern CAMEL_CASE_COMPONENT_RE = Pattern.compile("[A-Z][^A-Z]+");

  /**
   * Constructor used by subclasses whose name ends with "Function" and that
   * accept a single, or a variable number of arguments.
   *
   * @throws FunctionConfigurationException when the function name cannot be produced from the class name
   */
  public BaseFunction(ArgumentConstraint argumentConstraints) {
    this(null, argumentConstraints);
  }

  /**
   * Constructor used by subclasses whose name ends with "Function" and that
   * accept a fixed number of argument.
   *
   * @throws FunctionConfigurationException when the function name cannot be produced from the class name
   */
  public BaseFunction(ArgumentConstraint... argumentConstraints) {
    this(null, ArgumentConstraints.listOf(argumentConstraints));
  }

  /**
   * Constructor used by subclasses that provide a custom name (not based on
   * the class name) and that accept a fixed number of arguments.
   */
  public BaseFunction(String name, ArgumentConstraint... argumentConstraints) {
    this(name, ArgumentConstraints.listOf(argumentConstraints));
  }

  /**
   * Constructor used by subclasses that provide a custom name (not based on
   * the class name) and that accept a single, or a variable number of arguments.
   */
  public BaseFunction(String name, ArgumentConstraint argumentConstraints) {
    this.name = name == null ? classNameToFunctionName() : name;
    this.argumentConstraints = argumentConstraints;
  }

  private String classNameToFunctionName() {
    String n = getClass().getName();
    if (n.indexOf('$') > -1) {
      n = n.substring(n.lastIndexOf('$') + 1);
    } else {
      n = n.substring(n.lastIndexOf('.') + 1);
    }
    if (!n.endsWith("Function")) {
      throw new FunctionConfigurationException(String.format("The function defined by %s must either pass a name to the Function constructor or the class name must end with \"Function\"", getClass().getName()));
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
      snakeCaseName.append('_');
      offset = m.end();
    }
    snakeCaseName.deleteCharAt(snakeCaseName.length() - 1);
    return snakeCaseName.toString();
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public ArgumentConstraint argumentConstraints() {
    return argumentConstraints;
  }

  /**
   * Call this function with a list of arguments.
   *
   * The arguments can be either values or expressions, and will be checked
   * by the function's argument constraints before the function runs.
   */
  @Override
  public <T> T call(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    Iterator<FunctionArgument<T>> argumentIterator = arguments.iterator();
    Iterator<ArgumentError> maybeError = argumentConstraints.check(runtime, argumentIterator, true);
    if (!maybeError.hasNext()) {
      return callFunction(runtime, arguments);
    } else {
      ArgumentError error = maybeError.next();
      if (error instanceof ArgumentError.ArgumentTypeError) {
        ArgumentError.ArgumentTypeError e = (ArgumentError.ArgumentTypeError) error;
        return runtime.handleArgumentTypeError(this, e.expectedType(), e.actualType());
      } else if (error instanceof ArgumentError.ArityError) {
        throw new IllegalStateException(ArityException.createMessage(this, arguments.size(), true));
      } else {
        throw new IllegalStateException(String.format("Unexpected error while type checking arguments to \"%s\": %s", name(), error.getClass().getName()));
      }
    }
  }

  /**
   * Called from {@link #call} after the argument constraints have been checked
   * against the arguments.
   * <p>
   * May perform additional type checking and throw {@link ArgumentTypeException}.
   * For example when using expressions there is currently no way to check the
   * types produced by those expressions without running the function. Functions
   * that accept expressions are responsible for checking the types of the values
   * produced by those expressions.
   */
  protected abstract <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments);
}
