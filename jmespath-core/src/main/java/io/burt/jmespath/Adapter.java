package io.burt.jmespath;

import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Collection;

import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.function.Function;
import io.burt.jmespath.node.NodeFactory;

/**
 * An adapter helps the JMESPath parser and interpreter work with a JSON-like
 * structure without having to know how it works. Implement this interface
 * to make it possible to query any JSON-like structure with JMESPath.
 */
public interface Adapter<T> extends JmesPath<T>, Comparator<T> {
  /**
   * Parse a JSON string to a value.
   *
   * May throw exceptions when the given string is not valid JSON. The exact
   * exceptions thrown will depend on the concrete implementation.
   */
  T parseString(String str);

  /**
   * Converts the argument to a {@link List}.
   *
   * When the argument represents an array the list will contain the elements of
   * the array, when the argument represents an object the result is a list of
   * the object's values and for all other types the result is an empty list.
   */
  List<T> toList(T value);

  /**
   * Converts the argument to a {@link String}. When the argument represents a string
   * its string value is returned, otherwise a string with the value encoded
   * as JSON is returned.
   */
  String toString(T value);

  /**
   * Converts the argument to a {@link Number}, or null if the argument does not
   * represent a number.
   */
  Number toNumber(T value);

  /**
   * Returns true when the argument is truthy.
   *
   * All values are truthy, except the following, as per the JMESPath
   * specification: <code>false</code>, <code>null</code>, empty lists, empty
   * objects, empty strings.
   */
  boolean isTruthy(T value);

  /**
   * Returns the JSON type of the argument.
   *
   * As per the JMESPath specification the types are: <code>number</code>,
   * <code>string</code>, <code>boolean</code>, <code>array</code>,
   * <code>object</code>, <code>null</code>.
   *
   * @see JmesPathType
   */
  JmesPathType typeOf(T value);

  /**
   * Returns the value of a property of an object.
   *
   * The first argument must be an object and the second argument may be
   * the name of a property on that object. When the property does not exist
   * a null value (<em>not</em> Java <code>null</code>) is returned.
   */
  @Deprecated
  T getProperty(T value, String name);

  /**
   * Returns the value of a property of an object.
   *
   * The first argument must represent an object and the second argument may be
   * the name (which must be a string value) of a property of that object.
   * When the property does not exist or represents null a null value (but not
   * Java null) is returned.
   */
  T getProperty(T value, T name);

  /**
   * Returns all the property names of the given object, or an empty collection
   * when the given value does not represent an object.
   *
   * The property names are always string values.
   */
  Collection<T> getPropertyNames(T value);

  /**
   * Returns a null value (<em>not</em> Java <code>null</code>).
   */
  T createNull();

  /**
   * Returns an array value with the specified elements.
   */
  T createArray(Collection<T> elements);

  /**
   * Returns a string value containing the specified string.
   */
  T createString(String str);

  /**
   * Returns a boolean value containing the specified boolean.
   */
  T createBoolean(boolean b);

  /**
   * Returns an object value with the specified properties.
   *
   * The map keys must all be string values.
   *
   * Creating nested objects is not supported.
   */
  T createObject(Map<T, T> obj);

  /**
   * Returns a number value containing the specified floating point number.
   */
  T createNumber(double n);

  /**
   * Returns a number value containing the specified integer.
   */
  T createNumber(long n);

  /**
   * Throws an exception or returns a fallback value when a type check fails
   * during a function call evaluation.
   */
  T handleArgumentTypeError(Function function, String expectedType, String actualType);

  /**
   * Returns a function registry that can be used by the expression compiler
   * to look up functions.
   */
  FunctionRegistry functionRegistry();

  /**
   * Returns a node factory that can be used by the expression compiler to build
   * the interpreter AST.
   */
  NodeFactory<T> nodeFactory();
}
