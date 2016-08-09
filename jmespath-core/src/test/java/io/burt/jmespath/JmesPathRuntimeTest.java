package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import io.burt.jmespath.Query;
import io.burt.jmespath.function.FunctionCallException;
import io.burt.jmespath.function.ArityException;
import io.burt.jmespath.function.ArgumentTypeException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;

@SuppressWarnings("unchecked")
public abstract class JmesPathRuntimeTest<T> {
  protected T contact;
  protected T cloudtrail;

  protected abstract JmesPathRuntime<T> runtime();

  protected T loadExample(String path) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
      }
      return runtime().parseString(buffer.toString());
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Failed parsing %s", path), ioe);
    }
  }

  protected T evaluate(String query, T input) {
    return Query.fromString(runtime(), query).evaluate(runtime(), input);
  }

  protected Matcher<T> jsonBoolean(final boolean b) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return runtime().typeOf(node) == JmesPathType.BOOLEAN && runtime().isTruthy(node) == b;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON boolean with value ").appendValue(b);
      }
    };
  }

  protected Matcher<T> jsonNumber(final Number e) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T actual = (T) n;
        T expected = runtime().createNumber(e.doubleValue());
        return runtime().typeOf(actual) == JmesPathType.NUMBER && runtime().compare(actual, expected) == 0;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON number with value ").appendValue(e);
      }
    };
  }

  protected Matcher<T> jsonNull() {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return runtime().typeOf(node) == JmesPathType.NULL;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON null");
      }
    };
  }

  protected Matcher<T> jsonString(final String str) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return runtime().createString(str).equals(node);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON string with value ").appendValue(str);
      }
    };
  }

  protected Matcher<T> jsonArrayOfStrings(final String... strs) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        List<T> input = runtime().toList((T) n);
        if (input.size() != strs.length) {
          return false;
        }
        for (int i = 0; i < strs.length; i++) {
          if (!runtime().toString(input.get(i)).equals(strs[i])) {
            return false;
          }
        }
        return true;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON array ").appendValue(strs);
      }
    };
  }

  @Before
  public void loadExamples() {
    contact = loadExample("/contact.json");
    cloudtrail = loadExample("/cloudtrail.json");
  }

  @Test
  public void topLevelProperty() {
    T result = evaluate("lastName", contact);
    assertThat(result, is(jsonString("Smith")));
  }

  @Test
  public void chainProperty() {
    T result = evaluate("address.state", contact);
    assertThat(result, is(jsonString("NY")));
  }

  @Test
  public void propertyNotFound() {
    T result = evaluate("address.country", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void nullValue() {
    T result = evaluate("spouse", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void index() {
    T result = evaluate("phoneNumbers[1].type", contact);
    assertThat(result, is(jsonString("office")));
  }

  @Test
  public void negativeIndex() {
    T result = evaluate("phoneNumbers[-2].type", contact);
    assertThat(result, is(jsonString("office")));
  }

  @Test
  public void indexNotFound() {
    T result = evaluate("phoneNumbers[3].type", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void negativeIndexNotFound() {
    T result = evaluate("phoneNumbers[-4].type", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void indexOnNonArrayProducesNull() {
    T result = evaluate("[0]", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void projection() {
    T result = evaluate("phoneNumbers[*].type", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "office", "mobile")));
  }

  @Test
  public void multiStepProjection() {
    T result = evaluate("Records[*].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void projectionFiltersNull() {
    T result = evaluate("Records[*].requestParameters.keyName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("mykeypair")));
  }

  @Test
  public void projectionOnNonArrayProducesNull() {
    T result = evaluate("[*]", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void pipeStopsProjections() {
    T result = evaluate("Records[*].userIdentity | [1].userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void literalString() {
    T result = evaluate("'hello world'", cloudtrail);
    assertThat(result, is(jsonString("hello world")));
  }

  @Test
  public void literalStringIgnoresSource() {
    T result = evaluate("Records[*] | 'hello world'", cloudtrail);
    assertThat(result, is(jsonString("hello world")));
  }

  public void flattenStartsProjection() {
    T result = evaluate("Records[].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void flattenArray() {
    T nestedArray = runtime().parseString("[[0, 1, 2]]");
    T result = evaluate("[]", nestedArray);
    assertThat(result, is(runtime().parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenNonArrayProducesNull() {
    T result = evaluate("Records[0].userIdentity.userName[]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void flattenMultipleTimes() {
    T nestedArray = runtime().parseString("[[0, 1, 2]]");
    T result = evaluate("[][][][][][][][][][][][][]", nestedArray);
    assertThat(result, is(runtime().parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenInProjection() {
    T nestedArray = runtime().parseString("[{\"a\":[0]},{\"a\":[1]}]");
    T result = evaluate("[*].a[]", nestedArray);
    assertThat(result, is(runtime().parseString("[0, 1]")));
  }

  @Test
  public void flattenObject() {
    T result = evaluate("Records[0].userIdentity.*", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE", "123456789012", "Alice")));
  }

  @Test
  public void flattenObjectCreatesProjection() {
    T result = evaluate("Records[0].responseElements.*.items[].instanceId", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("i-ebeaf9e2")));
  }

  @Test
  public void multipleFlattenObject() {
    T nestedObject = runtime().parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result = evaluate("*.*", nestedObject);
    assertThat(result, is(runtime().parseString("[[{\"inner\":1}],[{\"inner\":2}]]")));
  }

  @Test
  @Ignore("How can the result in the first assertion be the basis for the result in the second?")
  public void multipleFlattenObjectWithFollowingProjection() {
    T nestedObject = runtime().parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result1 = evaluate("*.*.inner", nestedObject);
    assertThat(result1, is(runtime().parseString("[[1],[2]]")));
    T result = evaluate("*.*.inner[]", nestedObject);
    assertThat(result, is(runtime().parseString("[1,2]")));
  }

  @Test
  public void flattenNonObjectProducesNull() {
    T result = evaluate("Records[0].responseElements.instancesSet.items.*", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void slice() {
    T result = evaluate("Records[0].userIdentity.* | [1::2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("EX_PRINCIPAL_ID", "EXAMPLE_KEY_ID_ALICE", "Alice")));
  }

  @Test
  public void sliceNotFound() {
    T result = evaluate("Records[0].userIdentity.* | [99:]", cloudtrail);
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void negativeStopSlice() {
    T result = evaluate("Records[0].userIdentity.* | [:-2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE")));
  }

  @Test
  public void negativeStartSlice() {
    T result = evaluate("Records[0].userIdentity.* | [-3:4]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("EXAMPLE_KEY_ID_ALICE")));
  }

  @Test
  public void negativeStepSliceReversesOrder() {
    T result = evaluate("Records[0].userIdentity.* | [::-2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "EXAMPLE_KEY_ID_ALICE", "EX_PRINCIPAL_ID")));
  }

  @Test
  public void currentNodeReturnsInput() {
    T result = evaluate("@", cloudtrail);
    assertThat(runtime().toList(runtime().getProperty(result, "Records")), hasSize(3));
  }

  @Test
  public void currentNodeAsNoOp() {
    T result = evaluate("@ | Records[0].userIdentity | @ | userName | @ | @", cloudtrail);
    assertThat(result, is(jsonString("Alice")));
  }

  @Test
  public void andReturnsSecondOperandWhenFirstIsTruthy() {
    T result = evaluate("Records[0].userIdentity.userName && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void andReturnsFirstOperandWhenItIsFalsy() {
    T result = evaluate("'' && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("")));
  }

  @Test
  public void aLongChainOfAnds() {
    T result = evaluate("@ && Records[2] && Records[2].responseElements && Records[2].responseElements.keyName", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test
  public void orReturnsFirstOperandWhenItIsTruthy() {
    T result = evaluate("Records[0].userIdentity.userName || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Alice")));
  }

  @Test
  public void orReturnsSecondOperandWhenFirstIsFalsy() {
    T result = evaluate("'' || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void aLongChainOfOrs() {
    T result = evaluate("'' || Records[3] || Records[2].foobar || Records[2].responseElements.keyName", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test
  public void selectionWithTrueTest() {
    T result = evaluate("Records[?@]", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), hasSize(3));
  }

  @Test
  public void selectionWithBooleanProperty() {
    T result = evaluate("Records[*] | [?userIdentity.sessionContext.attributes.mfaAuthenticated].eventTime", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("2014-03-06T17:10:34Z")));
  }

  @Test
  public void selectionWithFalseTest() {
    T result = evaluate("Records[?'']", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void selectionStartsProjection() {
    T result = evaluate("Records[?@].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void selectionTestReferencingProperty() {
    T result = evaluate("Records[*].responseElements | [?keyFingerprint]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(elements, hasSize(1));
    assertThat(runtime().getProperty(elements.get(0), "keyName"), is(jsonString("mykeypair")));
  }

  @Test
  public void selectionDoesNotSelectProjectionPutEachProjectedElement() {
    T result = evaluate("Records[*].responseElements.keyName[?@]", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void selectionOnNonArrayProducesNull() {
    T result = evaluate("Records[0].userIdentity[?@]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void selectionWithComplexTest() {
    T result = evaluate("Records[*] | [?userIdentity.userName == 'Bob' || responseElements.instancesSet.items[0].instanceId == 'i-ebeaf9e2'].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob")));
  }

  @Test
  public void compareEqualityWhenEqualProducesTrue() {
    T result = evaluate("Records[0].userIdentity.userName == Records[2].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareEqualityWhenNotEqualProducesFalse() {
    T result = evaluate("Records[0].userIdentity.userName == Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNonEqualityWhenEqualProducesFalse() {
    T result = evaluate("Records[0].userIdentity.userName != Records[2].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNonEqualityWhenNotEqualProducesTrue() {
    T result = evaluate("Records[0].userIdentity.userName != Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersEqWhenEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code == currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersEqWhenNotEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code == previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersNotEqWhenEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code != currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersNotEqWhenNotEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code != previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGtWhenGt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code > previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGtWhenLt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code > currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersGteWhenGt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code >= previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGteWhenEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code >= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGteWhenLt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code >= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLtWhenGt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code < previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLtWhenLt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code < currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersLteWhenGt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code <= previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLteWhenEq() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code <= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersLteWhenLt() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code <= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareGtWithNonNumberProducesNull() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState > currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareGteWithNonNumberProducesNull() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState >= currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareLtWithNonNumberProducesNull() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState < currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareLteWithNonNumberProducesNull() {
    T result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState <= currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void negateSomethingTruthyProducesFalse() {
    T result = evaluate("!'hello'", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void negateNullProducesTrue() {
    T result = evaluate("!Records[3]", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void negateEmptyStringProducesTrue() {
    T result = evaluate("!''", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void negateEmptyArrayProducesTrue() {
    T result = evaluate("Records[?''] | !@", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void createObject() {
    T result = evaluate("{userNames: Records[*].userIdentity.userName, keyName: Records[2].responseElements.keyName}", cloudtrail);
    T userNames = runtime().getProperty(result, "userNames");
    T keyName = runtime().getProperty(result, "keyName");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(keyName, is(jsonString("mykeypair")));
  }

  @Test
  public void createObjectInPipe() {
    T result = evaluate("Records[*].userIdentity | {userNames: [*].userName, anyUsedMfa: ([?sessionContext.attributes.mfaAuthenticated] | !!@)}", cloudtrail);
    T userNames = runtime().getProperty(result, "userNames");
    T anyUsedMfa = runtime().getProperty(result, "anyUsedMfa");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(anyUsedMfa, is(jsonBoolean(true)));
  }

  @Test
  public void createObjectInProjection() {
    T result = evaluate("Records[*].userIdentity.{userName: userName, usedMfa: sessionContext.attributes.mfaAuthenticated}", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().getProperty(elements.get(0), "usedMfa"), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(1), "usedMfa"), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(2), "usedMfa"), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateObject() {
    T result = evaluate("Records[*].userIdentity | {users: {names: [*].userName}}", cloudtrail);
    T names = runtime().getProperty(runtime().getProperty(result, "users"), "names");
    assertThat(names, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void createObjectOnNullProducesNull() {
    T result = evaluate("bork.{foo: bar}", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void createArray() {
    T result = evaluate("[Records[*].userIdentity.userName, Records[2].responseElements.keyName]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonString("mykeypair")));
  }

  @Test
  public void createArrayInPipe() {
    T result = evaluate("Records[*].userIdentity | [[*].userName, ([?sessionContext.attributes.mfaAuthenticated] | !!@)]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonBoolean(true)));
  }

  @Test
  public void createArrayInProjection() {
    T result = evaluate("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().toList(elements.get(0)).get(1), is(jsonNull()));
    assertThat(runtime().toList(elements.get(1)).get(1), is(jsonNull()));
    assertThat(runtime().toList(elements.get(2)).get(1), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateArray() {
    T result = evaluate("Records[*].userIdentity | [[*].type, [[*].userName]]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("IAMUser", "IAMUser", "IAMUser")));
    assertThat(runtime().toList(elements.get(1)).get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void createArrayOnNullProducesNull() {
    T result = evaluate("bork.[snork]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void jsonLiteralNumber() {
    T result = evaluate("`42`", runtime().parseString("{}"));
    assertThat(result, is(runtime().parseString("42")));
  }

  @Test
  public void jsonLiteralString() {
    T result = evaluate("`\"foo\"`", runtime().parseString("{}"));
    assertThat(result, is(jsonString("foo")));
  }

  @Test
  public void jsonLiteralBoolean() {
    T result = evaluate("`true`", runtime().parseString("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void jsonLiteralArray() {
    T result = evaluate("`[42, \"foo\", true]`", runtime().parseString("{}"));
    assertThat(result, is(runtime().parseString("[42, \"foo\", true]")));
  }

  @Test
  public void jsonLiteralObject() {
    T result = evaluate("`{\"n\": 42, \"s\": \"foo\", \"b\": true}`", runtime().parseString("{}"));
    assertThat(result, is(runtime().parseString("{\"n\": 42, \"s\": \"foo\", \"b\": true}")));
  }

  @Test
  public void jsonLiteralInComparison() {
    T result = evaluate("Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("72.21.198.64")));
  }

  @Test
  public void comparingJsonLiteralsWithRawContents() {
    Query query = Query.fromString(null, "Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress");
    T result = query.evaluate(runtime(), cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("72.21.198.64")));
  }

  @Test
  public void numbersAreTruthy() {
    T result = evaluate("!@", runtime().parseString("1"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void stringsAreTruthy() {
    T result = evaluate("!@", runtime().parseString("\"foo\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void nonEmptyArraysAreTruthy() {
    T result = evaluate("!@", runtime().parseString("[\"foo\"]"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void nonEmptyObjectsAreTruthy() {
    T result = evaluate("!@", runtime().parseString("{\"foo\":3}"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void trueIsTruthy() {
    T result = evaluate("!@", runtime().parseString("true"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void falseIsNotTruthy() {
    T result = evaluate("!@", runtime().parseString("false"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void nullIsNotTruthy() {
    T result = evaluate("!@", runtime().parseString("null"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyStringIsNotTruthy() {
    T result = evaluate("!@", runtime().parseString("\"\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyArrayIsNotTruthy() {
    T result = evaluate("!@", runtime().parseString("[]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyObjectIsNotTruthy() {
    T result = evaluate("!@", runtime().parseString("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void callFunction() {
    T result = evaluate("type(@)", runtime().parseString("{}"));
    assertThat(result, is(jsonString("object")));
  }

  @Test
  public void callFunctionWithExpressionReference() {
    T result = evaluate("map(&userIdentity.userName, Records)", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void callVariadicFunction() {
    T result = evaluate("not_null(Records[0].requestParameters.keyName, Records[1].requestParameters.keyName, Records[2].requestParameters.keyName)", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test(expected = FunctionCallException.class)
  public void callNonExistentFunctionThrowsFunctionCallException() {
    evaluate("bork()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void callFunctionWithTooFewArgumentsThrowsArityException() {
    evaluate("type()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void callFunctionWithTooManyArgumentsThrowsArityException() {
    evaluate("type(@, @, @)", runtime().parseString("{}"));
  }

  @Test
  public void absReturnsTheAbsoluteValueOfANumber() {
    T result1 = evaluate("abs(`-1`)", runtime().parseString("{}"));
    T result2 = evaluate("abs(`1`)", runtime().parseString("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(1)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void absRequiresANumberArgument() {
    evaluate("abs('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void absRequiresExactlyOneArgument() {
    evaluate("abs(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void absRequiresAValue() {
    evaluate("abs(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void avgReturnsTheAverageOfAnArrayOfNumbers() {
    T result = evaluate("avg(`[0, 1, 2, 3.5, 4]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(2.1)));
  }

  @Test
  public void avgReturnsNullWhenGivenAnEmptyArray() {
    T result = evaluate("avg(`[]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void avgRequiresAnArrayOfNumbers() {
    evaluate("avg('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void avgRequiresExactlyOneArgument() {
    evaluate("avg(`[]`, `[]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void avgRequiresAValue() {
    evaluate("avg(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void containsReturnsTrueWhenTheNeedleIsFoundInTheHaystack() {
    T result = evaluate("contains(@, `3`)", runtime().parseString("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsComparesDeeply() {
    T result = evaluate("contains(@, `[\"bar\", {\"baz\": 42}]`)", runtime().parseString("[1, 2, 3, \"foo\", [\"bar\", {\"baz\": 42}]]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsReturnsFalseWhenTheNeedleIsNotFoundInTheHaystack() {
    T result = evaluate("contains(@, `4`)", runtime().parseString("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void containsSearchesInStrings() {
    T result = evaluate("contains('hello', 'hell')", runtime().parseString("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments() {
    evaluate("contains(@)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresAnArrayOrStringAsFirstArgument() {
    evaluate("contains(@, 'foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments1() {
    evaluate("contains('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments2() {
    evaluate("contains('foo', 'bar', 'baz')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresValues1() {
    evaluate("contains(@, &foo)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresValues2() {
    evaluate("contains(&foo, 'bar')", runtime().parseString("{}"));
  }

  @Test
  public void ceilReturnsTheNextWholeNumber() {
    T result1 = evaluate("ceil(`0.9`)", runtime().parseString("{}"));
    T result2 = evaluate("ceil(`33.3`)", runtime().parseString("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(34)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void ceilRequiresANumberArgument() {
    evaluate("ceil('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void ceilRequiresExactlyOneArgument() {
    evaluate("ceil(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void ceilRequiresAValue() {
    evaluate("ceil(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void endsWithReturnsTrueWhenTheFirstArgumentEndsWithTheSecond() {
    T result = evaluate("ends_with(@, 'rld')", runtime().parseString("\"world\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void endsWithReturnsFalseWhenTheFirstArgumentDoesNotEndWithTheSecond() {
    T result = evaluate("ends_with(@, 'rld')", runtime().parseString("\"hello\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments() {
    evaluate("ends_with('')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAStringAsFirstArgument() {
    evaluate("ends_with(@, 'foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAStringAsSecondArgument() {
    evaluate("ends_with('foo', @)", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments1() {
    evaluate("ends_with('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments2() {
    evaluate("ends_with('foo', 'bar', @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAValue1() {
    evaluate("ends_with(&foo, 'bar')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAValue2() {
    evaluate("ends_with('foo', &bar)", runtime().parseString("{}"));
  }

  @Test
  public void floorReturnsThePreviousWholeNumber() {
    T result1 = evaluate("floor(`0.9`)", runtime().parseString("{}"));
    T result2 = evaluate("floor(`33.3`)", runtime().parseString("{}"));
    assertThat(result1, is(jsonNumber(0)));
    assertThat(result2, is(jsonNumber(33)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void floorRequiresANumberArgument() {
    evaluate("floor('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void floorRequiresExactlyOneArgument() {
    evaluate("floor(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void floorRequiresAValue() {
    evaluate("floor(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void joinSmashesAnArrayOfStringsTogether() {
    T result = evaluate("join('|', @)", runtime().parseString("[\"foo\", \"bar\", \"baz\"]"));
    assertThat(result, is(jsonString("foo|bar|baz")));
  }

  @Test
  public void joinHandlesDuplicates() {
    T string = runtime().createString("foo");
    T value = runtime().createArray(Arrays.asList(string, string, string));
    T result = evaluate("join('|', @)", value);
    assertThat(result, is(jsonString("foo|foo|foo")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAStringAsFirstArgument() {
    evaluate("join(`3`, @)", runtime().parseString("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAStringArrayAsSecondArgument() {
    evaluate("join('|', @)", runtime().parseString("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = ArityException.class)
  public void joinRequiresTwoArguments1() {
    evaluate("join('|')", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void joinRequiresTwoArguments2() {
    evaluate("join('|', @, @)", runtime().parseString("[]"));
  }

  @Test
  public void joinWithAnEmptyArrayReturnsAnEmptyString() {
    T result = evaluate("join('|', @)", runtime().parseString("[]"));
    assertThat(result, is(jsonString("")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAValue1() {
    evaluate("join(&foo, @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAValue2() {
    evaluate("join('foo', &bar)", runtime().parseString("{}"));
  }

  @Test
  public void keysReturnsTheNamesOfAnObjectsProperties() {
    T result = evaluate("keys(@)", runtime().parseString("{\"foo\":3,\"bar\":4}"));
    assertThat(result, is(jsonArrayOfStrings("foo", "bar")));
  }

  @Test
  public void keysReturnsAnEmptyArrayWhenGivenAnEmptyObject() {
    T result = evaluate("keys(@)", runtime().parseString("{}"));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void keysRequiresAnObjectAsArgument() {
    evaluate("keys(@)", runtime().parseString("[3]"));
  }

  @Test(expected = ArityException.class)
  public void keysRequiresASingleArgument() {
    evaluate("keys(@, @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void keysRequiresAValue() {
    evaluate("keys(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void lengthReturnsTheLengthOfAString() {
    T result = evaluate("length(foo)", runtime().parseString("{\"foo\":\"bar\"}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void lengthReturnsTheSizeOfAnArray() {
    T result = evaluate("length(foo)", runtime().parseString("{\"foo\":[0, 1, 2, 3]}"));
    assertThat(result, is(jsonNumber(4)));
  }

  @Test
  public void lengthReturnsTheSizeOfAnObject() {
    T result = evaluate("length(@)", runtime().parseString("{\"foo\":[0, 1, 2, 3]}"));
    assertThat(result, is(jsonNumber(1)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void lengthRequiresAStringArrayOrObjectAsArgument() {
    evaluate("length(@)", runtime().parseString("3"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void lengthRequiresAValue() {
    evaluate("length(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void mapTransformsAnArrayIntoAnAnotherArrayByApplyingAnExpressionToEachElement() {
    T result = evaluate("map(&type, phoneNumbers)", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "office", "mobile")));
  }

  @Test
  public void mapReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = evaluate("map(&foo, @)", runtime().parseString("[]"));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnExpressionAsFirstArgument() {
    evaluate("map(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnArrayAsSecondArgument1() {
    evaluate("map(&foo, @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnArrayAsSecondArgument2() {
    evaluate("map(@, &foo)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void mapRequiresTwoArguments1() {
    evaluate("map(&foo.bar)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void mapRequiresTwoArguments2() {
    evaluate("map(&foo.bar, @, @)", runtime().parseString("[]"));
  }

  @Test
  public void maxReturnsTheGreatestOfAnArrayOfNumbers() {
    T result = evaluate("max(`[0, 1, 4, 3.5, 2]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(4)));
  }

  @Test
  public void maxReturnsTheGreatestOfAnArrayOfStrings() {
    T result = evaluate("max(`[\"a\", \"d\", \"b\"]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonString("d")));
  }

  @Test
  public void maxReturnsNullWhenGivenAnEmptyArray() {
    T result = evaluate("max(`[]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresAnArrayOfNumbersOrStrings() {
    evaluate("max('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresTheElementsToBeOfTheSameType() {
    evaluate("max(`[\"foo\", 1]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void maxRequiresExactlyOneArgument() {
    evaluate("max(`[]`, `[]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresAValue() {
    evaluate("max(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void maxByReturnsTheElementWithTheGreatestValueForAnExpressionThatReturnsStrings() {
    T result = evaluate("max_by(phoneNumbers, &type)", contact);
    assertThat(result, is(runtime().parseString("{\"type\": \"office\", \"number\": \"646 555-4567\"}")));
  }

  @Test
  public void maxByReturnsTheElementWithTheGreatestValueForAnExpressionThatReturnsNumbers() {
    T result = evaluate("max_by(@, &foo)", runtime().parseString("[{\"foo\": 3}, {\"foo\": 6}, {\"foo\": 1}]"));
    assertThat(result, is(runtime().parseString("{\"foo\": 6}")));
  }

  @Test
  public void maxByReturnsWithAnEmptyArrayReturnsNull() {
    T result = evaluate("max_by(@, &foo)", runtime().parseString("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByDoesNotAcceptMixedResults() {
    evaluate("max_by(@, &foo)", runtime().parseString("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByDoesNotAcceptNonStringsOrNumbers() {
    evaluate("max_by(@, &foo)", runtime().parseString("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnArrayAsFirstArgument1() {
    evaluate("max_by(@, &foo)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnArrayAsFirstArgument2() {
    evaluate("max_by(&foo, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnExpressionAsSecondArgument() {
    evaluate("max_by(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void maxByRequiresTwoArguments1() {
    evaluate("max_by(@)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void maxByRequiresTwoArguments2() {
    evaluate("max_by(@, &foo, @)", runtime().parseString("[]"));
  }

  @Test
  public void mergeMergesObjects() {
    T result = evaluate("merge(foo, bar)", runtime().parseString("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(runtime().parseString("{\"a\": 1, \"b\": 2}")));
  }

  @Test
  public void mergeReturnsTheArgumentWhenOnlyGivenOne() {
    T result = evaluate("merge(foo)", runtime().parseString("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(runtime().parseString("{\"a\": 1, \"b\": 1}}")));
  }

  @Test
  public void mergeDoesNotMutate() {
    T result = evaluate("merge(foo, bar) && foo", runtime().parseString("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(runtime().parseString("{\"a\": 1, \"b\": 1}")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresObjectArguments1() {
    evaluate("merge('foo', 'bar')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresObjectArguments2() {
    evaluate("merge(`{}`, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void mergeRequiresAtLeastOneArgument() {
    evaluate("merge()", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresAValue() {
    evaluate("merge(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void minReturnsTheGreatestOfAnArrayOfNumbers() {
    T result = evaluate("min(`[0, 1, -4, 3.5, 2]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(-4)));
  }

  @Test
  public void minReturnsTheGreatestOfAnArrayOfStrings() {
    T result = evaluate("min(`[\"foo\", \"bar\"]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonString("bar")));
  }

  @Test
  public void minReturnsNullWhenGivenAnEmptyArray() {
    T result = evaluate("min(`[]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresAnArrayOfNumbersOrStrings() {
    evaluate("min('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresTheElementsToBeOfTheSameType() {
    evaluate("min(`[\"foo\", 1]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void minRequiresExactlyOneArgument() {
    evaluate("min(`[]`, `[]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresAValue() {
    evaluate("min(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void minByReturnsTheElementWithTheLeastValueForAnExpressionThatReturnsStrings() {
    T result = evaluate("min_by(phoneNumbers, &type)", contact);
    assertThat(result, is(runtime().parseString("{\"type\": \"home\",\"number\": \"212 555-1234\"}")));
  }

  @Test
  public void minByReturnsTheElementWithTheLeastValueForAnExpressionThatReturnsNumbers() {
    T result = evaluate("min_by(@, &foo)", runtime().parseString("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(runtime().parseString("{\"foo\": -6}")));
  }

  @Test
  public void minByReturnsWithAnEmptyArrayReturnsNull() {
    T result = evaluate("min_by(@, &foo)", runtime().parseString("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByDoesNotAcceptMixedResults() {
    evaluate("min_by(@, &foo)", runtime().parseString("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByDoesNotAcceptNonStringsOrNumbers() {
    evaluate("min_by(@, &foo)", runtime().parseString("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnArrayAsFirstArgument1() {
    evaluate("min_by(@, &foo)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnArrayAsFirstArgument2() {
    evaluate("min_by(&foo, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnExpressionAsSecondArgument() {
    evaluate("min_by(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void minByRequiresTwoArguments1() {
    evaluate("min_by(@)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void minByRequiresTwoArguments2() {
    evaluate("min_by(@, &foo, @)", runtime().parseString("[]"));
  }

  @Test
  public void notNullReturnsTheFirstNonNullArgument() {
    T result = evaluate("not_null(`null`, `null`, `3`, `null`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void notNullReturnsNullWhenGivenOnlyNull() {
    T result = evaluate("not_null(`null`, `null`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArityException.class)
  public void notNullRequiresAtLeastOneArgument() {
    evaluate("not_null()", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void notNullRequiresAValue() {
    evaluate("not_null(`null`, &foo)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  @Ignore("Not sure if this should be an error or not")
  public void notNullRequiresAValueForArgumentsThatAreNotInspected() {
    evaluate("not_null('foo', &foo)", runtime().parseString("{}"));
  }

  @Test
  public void reverseReversesAnArray() {
    T result = evaluate("reverse(@)", runtime().parseString("[\"foo\", 3, 2, 1]"));
    assertThat(result, is(runtime().parseString("[1, 2, 3, \"foo\"]")));
  }

  @Test
  public void reverseReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = evaluate("reverse(@)", runtime().parseString("[]"));
    assertThat(result, is(runtime().parseString("[]")));
  }

  @Test
  public void reverseReversesAString() {
    T result = evaluate("reverse('hello world')", runtime().parseString("{}"));
    assertThat(result, is(jsonString("dlrow olleh")));
  }

  @Test
  public void reverseReturnsAnEmptyStringWhenGivenAnEmptyString() {
    T result = evaluate("reverse('')", runtime().parseString("{}"));
    assertThat(result, is(jsonString("")));
  }

  @Test(expected = ArityException.class)
  public void reverseRequiresOneArgument1() {
    evaluate("reverse()", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void reverseRequiresOneArgument2() {
    evaluate("reverse(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void reverseRequiresAnArrayAsArgument() {
    evaluate("reverse(@)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void reverseRequiresAValue() {
    evaluate("reverse(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void sortsSortsAnArrayOfNumbers() {
    T result = evaluate("sort(@)", runtime().parseString("[6, 7, 1]"));
    assertThat(result, is(runtime().parseString("[1, 6, 7]")));
  }

  @Test
  public void sortsHandlesDuplicates() {
    T result = evaluate("sort(@)", runtime().parseString("[6, 6, 7, 1, 1]"));
    assertThat(result, is(runtime().parseString("[1, 1, 6, 6, 7]")));
  }

  @Test
  public void sortsSortsAnArrayOfStrings() {
    T result = evaluate("sort(@)", runtime().parseString("[\"b\", \"a\", \"x\"]"));
    assertThat(result, is(runtime().parseString("[\"a\", \"b\", \"x\"]")));
  }

  @Test
  public void sortReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = evaluate("sort(@)", runtime().parseString("[]"));
    assertThat(result, is(runtime().parseString("[]")));
  }

  @Test(expected = ArityException.class)
  public void sortRequiresOneArgument1() {
    evaluate("sort()", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortRequiresOneArgument2() {
    evaluate("sort(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortRequiresAnArrayAsArgument() {
    evaluate("sort(@)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortDoesNotAcceptMixedInputs() {
    evaluate("sort(@)", runtime().parseString("[1, \"foo\"]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortRequiresAValue() {
    evaluate("sort(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void sortBySortsTheInputBasedOnStringsReturnedByAnExpression() {
    T result = evaluate("sort_by(phoneNumbers, &type)[*].type", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "mobile", "office")));
  }

  @Test
  public void sortBySortsTheInputBasedOnNumbersReturnedByAnExpression() {
    T result = evaluate("sort_by(@, &foo)[*].foo", runtime().parseString("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(runtime().parseString("[-6, 1, 3]")));
  }

  @Test
  public void sortByHandlesDuplicates() {
    T result = evaluate("sort_by(@, &foo)[*].foo", runtime().parseString("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(runtime().parseString("[-6, -6, 1, 3]")));
  }

  @Test
  public void sortBySortsIsStable() {
    T result = evaluate("sort_by(@, &foo)[*].x", runtime().parseString("[{\"foo\": 3, \"x\": 3}, {\"foo\": 3, \"x\": 1}, {\"foo\": 1}]"));
    assertThat(result, is(runtime().parseString("[3, 1]")));
  }

  @Test
  public void sortByReturnsWithAnEmptyArrayReturnsNull() {
    T result = evaluate("sort_by(@, &foo)", runtime().parseString("[]"));
    assertThat(result, is(runtime().parseString("[]")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByDoesNotAcceptMixedResults() {
    evaluate("sort_by(@, &foo)", runtime().parseString("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByDoesNotAcceptNonStringsOrNumbers() {
    evaluate("sort_by(@, &foo)", runtime().parseString("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnArrayAsFirstArgument1() {
    evaluate("sort_by(@, &foo)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnArrayAsFirstArgument2() {
    evaluate("sort_by(&foo, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnExpressionAsSecondArgument() {
    evaluate("sort_by(@, @)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortByRequiresTwoArguments1() {
    evaluate("sort_by(@)", runtime().parseString("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortByRequiresTwoArguments2() {
    evaluate("sort_by(@, &foo, @)", runtime().parseString("[]"));
  }

  @Test
  public void startsWithReturnsTrueWhenTheFirstArgumentEndsWithTheSecond() {
    T result = evaluate("starts_with(@, 'wor')", runtime().parseString("\"world\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void startsWithReturnsFalseWhenTheFirstArgumentDoesNotEndWithTheSecond() {
    T result = evaluate("starts_with(@, 'wor')", runtime().parseString("\"hello\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments() {
    evaluate("starts_with('')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAStringAsFirstArgument() {
    evaluate("starts_with(@, 'foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAStringAsSecondArgument() {
    evaluate("starts_with('foo', @)", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments1() {
    evaluate("starts_with('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments2() {
    evaluate("starts_with('foo', 'bar', @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAValue1() {
    evaluate("starts_with(&foo, 'bar')", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAValue2() {
    evaluate("starts_with('foo', &bar)", runtime().parseString("{}"));
  }

  @Test
  public void sumReturnsTheAverageOfAnArrayOfNumbers() {
    T result = evaluate("sum(`[0, 1, 2, 3.5, 4]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(10.5)));
  }

  @Test
  public void sumReturnsZeroWhenGivenAnEmptyArray() {
    T result = evaluate("sum(`[]`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(0)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sumRequiresAnArrayOfNumbers() {
    evaluate("sum('foo')", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void sumRequiresExactlyOneArgument() {
    evaluate("sum(`[]`, `[]`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sumRequiresAValue() {
    evaluate("sum(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void toArrayReturnsASingletonArrayWithTheArgument() {
    T result = evaluate("to_array(`34`)", runtime().parseString("{}"));
    assertThat(result, is(runtime().parseString("[34]")));
  }

  @Test
  public void toArrayWithAnArrayReturnsTheArgument() {
    T result = evaluate("to_array(@)", runtime().parseString("[0, 1, 2, 3.5, 4]"));
    assertThat(result, is(runtime().parseString("[0, 1, 2, 3.5, 4]")));
  }

  @Test(expected = ArityException.class)
  public void toArrayRequiresExactlyOneArgument1() {
    evaluate("to_array()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void toArrayRequiresExactlyOneArgument2() {
    evaluate("to_array(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toArrayRequiresAValue() {
    evaluate("to_array(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void toStringReturnsTheJsonEncodingOfTheArgument() {
    T input = runtime().parseString("{\"foo\": [1, 2, [\"bar\"]]}");
    T result = evaluate("to_string(@)", input);
    assertThat(runtime().toString(result), both(containsString("\"foo\"")).and(is(runtime().toString(input))));
  }

  @Test
  public void toStringWithAStringReturnsTheArgument() {
    T result = evaluate("to_string('hello')", runtime().parseString("{}"));
    assertThat(result, is(jsonString("hello")));
  }

  @Test(expected = ArityException.class)
  public void toStringRequiresExactlyOneArgument1() {
    evaluate("to_string()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void toStringRequiresExactlyOneArgument2() {
    evaluate("to_string(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toStringRequiresAValue() {
    evaluate("to_string(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void toNumberWithANumberReturnsTheArgument() {
    T result = evaluate("to_number(`3`)", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void toNumberParsesAnIntegerStringToANumber() {
    T result = evaluate("to_number('33')", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(33)));
  }

  @Test
  public void toNumberParsesAnFloatStringToANumber() {
    T result = evaluate("to_number('3.3')", runtime().parseString("{}"));
    assertThat(result, is(jsonNumber(3.3)));
  }

  @Test
  public void toNumberReturnsNullWhenGivenNonNumberString() {
    T result = evaluate("to_number('n=3.3')", runtime().parseString("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenAnArray() {
    T result = evaluate("to_number(@)", runtime().parseString("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenAnObject() {
    T result = evaluate("to_number(@)", runtime().parseString("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenABoolean() {
    T result = evaluate("to_number(@)", runtime().parseString("true"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenNull() {
    T result = evaluate("to_number(@)", runtime().parseString("null"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArityException.class)
  public void toNumberRequiresExactlyOneArgument1() {
    evaluate("to_number()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void toNumberRequiresExactlyOneArgument2() {
    evaluate("to_number(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toNumberRequiresAValue() {
    evaluate("to_number(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void typeReturnsTheTypeOfTheArgument() {
    assertThat(evaluate("type(@)", runtime().parseString("null")), is(jsonString("null")));
    assertThat(evaluate("type(@)", runtime().parseString("false")), is(jsonString("boolean")));
    assertThat(evaluate("type(@)", runtime().parseString("{\"foo\":3}")), is(jsonString("object")));
    assertThat(evaluate("type(@)", runtime().parseString("[3, 4]")), is(jsonString("array")));
    assertThat(evaluate("type(@)", runtime().parseString("\"foo\"")), is(jsonString("string")));
    assertThat(evaluate("type(@)", runtime().parseString("1")), is(jsonString("number")));
  }

  @Test(expected = ArityException.class)
  public void typeRequiresExactlyOneArgument1() {
    evaluate("type()", runtime().parseString("{}"));
  }

  @Test(expected = ArityException.class)
  public void typeRequiresExactlyOneArgument2() {
    evaluate("type(`1`, `2`)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void typeRequiresAValue() {
    evaluate("type(&foo)", runtime().parseString("{}"));
  }

  @Test
  public void valuesReturnsTheValuesOfAnObjectsProperties() {
    T result = evaluate("values(@)", runtime().parseString("{\"foo\":\"one\",\"bar\":\"two\"}"));
    assertThat(result, is(jsonArrayOfStrings("one", "two")));
  }

  @Test
  public void valuesReturnsAnEmptyArrayWhenGivenAnEmptyObject() {
    T result = evaluate("values(@)", runtime().parseString("{}"));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void valuesRequiresAnObjectAsArgument() {
    evaluate("values(@)", runtime().parseString("[3]"));
  }

  @Test(expected = ArityException.class)
  public void valuesRequiresASingleArgument() {
    evaluate("values(@, @)", runtime().parseString("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void valuesRequiresAValue() {
    evaluate("values(&foo)", runtime().parseString("{}"));
  }
}
