package io.burt.jmespath;

import java.util.List;
import java.util.Comparator;
import java.util.Map;
import java.util.Collection;

import io.burt.jmespath.function.ExpressionOrValue;

/**
 * An adapter helps the JMESPath parser and interpreter work with a JSON-like
 * structure without having to know how it works. Implement this interface
 * to make it possible to query any JSON-like structure with JMESPath.
 */
public interface Adapter<T> extends Comparator<T> {
  /**
   * Parse a JSON string to a value.
   */
  T parseString(String str);

  /**
   * Converts the argument to a List<T>, or an empty list when the argument does
   * not represent an array or object.
   *
   * When the argument is an object the result is the object's values.
   */
  List<T> toList(T value);

  /**
   * Converts the argument to a String. When the argument represents a string
   * its string value is returned, otherwise a string with the value encoded
   * as JSON is returned.
   */
  String toString(T value);

  /**
   * Converts the argument to a Number, or null if the argument does not
   * represent a number.
   */
  Number toNumber(T value);

  /**
   * Returns true when the argument is an array.
   */
  boolean isArray(T value);

  /**
   * Returns true when the argument is an object.
   */
  boolean isObject(T value);

  /**
   * Returns true when the argument is a boolean.
   */
  boolean isBoolean(T value);

  /**
   * Returns true when the argument is a number.
   */
  boolean isNumber(T value);

  /**
   * Returns true when the argument is truthy.
   *
   * All values are truthy, except the following, as per the JMESPath
   * specification: false, null, empty lists, empty objects, empty strings.
   */
  boolean isTruthy(T value);

  /**
   * Returns true when the argument represets null.
   */
  boolean isNull(T value);

  /**
   * Returns true when the argument represets a string.
   */
  boolean isString(T value);

  /**
   * Returns the JSON type of the argument.
   *
   * As per the JMESPath specification the types are: number, string, boolean,
   * array, object, null.
   */
  JmesPathType typeOf(T value);

  /**
   * Returns a property from an object.
   *
   * The first argument must be an object and the second argument may be
   * the name of a property on that object. When the property does not exist
   * or is null a null value (but not Java null) is returned.
   */
  T getProperty(T value, String name);

  /**
   * Returns a property from an object.
   *
   * The first argument must be an object and the second argument may be
   * the name (which must be a string value) of a property on that object.
   * When the property does not exist or is null a null value (but not Java
   * null) is returned.
   */
  T getProperty(T value, T name);

  /**
   * Returns all of the property names of the given object, or an empty list
   * when the given value is not an object.
   *
   * The property names are always string values.
   */
  Collection<T> getPropertyNames(T value);

  /**
   * Returns a null value (but not Java null).
   */
  T createNull();

  /**
   * Returns an array with the specified elements.
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
   * Returns an object with the specified properties.
   *
   * The map keys must be string values.
   *
   * Does not support creating nested objects.
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
   * Calls a function with the specified arguments and returns the result.
   *
   * Arguments can be either JMESPath expressions or values. How the arguments
   * are interpreted is up to the function.
   */
  T callFunction(String name, List<ExpressionOrValue<T>> arguments);
}
