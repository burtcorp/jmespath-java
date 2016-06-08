package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import io.burt.jmespath.Query;
import io.burt.jmespath.function.FunctionCallException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;

public abstract class AdapterTest<T> {
  protected T contact;
  protected T cloudtrail;

  protected abstract Adapter<T> adapter();

  protected T loadExample(String path) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
      StringBuffer buffer = new StringBuffer();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
      }
      return adapter().parseString(buffer.toString());
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Failed parsing %s", path), ioe);
    }
  }

  protected T evaluate(String query, T input) {
    return Query.fromString(adapter(), query).evaluate(adapter(), input);
  }

  protected Matcher<T> jsonBoolean(final boolean b) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return adapter().isBoolean(node) && adapter().isTruthy(node) == b;
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON boolean with value ").appendValue(b);
      }
    };
  }

  protected Matcher<T> jsonNumber(final double d) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return adapter().isNumber(node) && n.equals(adapter().createNumber(d));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("JSON number with value ").appendValue(d);
      }
    };
  }

  protected Matcher<T> jsonNull() {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(final Object n) {
        T node = (T) n;
        return adapter().isNull(node);
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
        return adapter().createString(str).equals(node);
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
        List<T> input = adapter().toList((T) n);
        if (input.size() != strs.length) {
          return false;
        }
        for (int i = 0; i < strs.length; i++) {
          if (!adapter().toString(input.get(i)).equals(strs[i])) {
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
    T nestedArray = adapter().parseString("[[0, 1, 2]]");
    T result = evaluate("[]", nestedArray);
    assertThat(result, is(adapter().parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenNonArrayProducesNull() {
    T result = evaluate("Records[0].userIdentity.userName[]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void flattenMultipleTimes() {
    T nestedArray = adapter().parseString("[[0, 1, 2]]");
    T result = evaluate("[][][][][][][][][][][][][]", nestedArray);
    assertThat(result, is(adapter().parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenInProjection() {
    T nestedArray = adapter().parseString("[{\"a\":[0]},{\"a\":[1]}]");
    T result = evaluate("[*].a[]", nestedArray);
    assertThat(result, is(adapter().parseString("[0, 1]")));
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
    T nestedObject = adapter().parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result = evaluate("*.*", nestedObject);
    assertThat(result, is(adapter().parseString("[[{\"inner\":1}],[{\"inner\":2}]]")));
  }

  @Test
  @Ignore("How can the result in the first assertion be the basis for the result in the second?")
  public void multipleFlattenObjectWithFollowingProjection() {
    T nestedObject = adapter().parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result1 = evaluate("*.*.inner", nestedObject);
    assertThat(result1, is(adapter().parseString("[[1],[2]]")));
    T result = evaluate("*.*.inner[]", nestedObject);
    assertThat(result, is(adapter().parseString("[1,2]")));
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
    assertThat(adapter().toList(result), is(empty()));
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
    assertThat(adapter().toList(adapter().getProperty(result, "Records")), hasSize(3));
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
    assertThat(adapter().isArray(result), is(true));
    assertThat(adapter().toList(result), hasSize(3));
  }

  @Test
  public void selectionWithBooleanProperty() {
    T result = evaluate("Records[*] | [?userIdentity.sessionContext.attributes.mfaAuthenticated].eventTime", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("2014-03-06T17:10:34Z")));
  }

  @Test
  public void selectionWithFalseTest() {
    T result = evaluate("Records[?'']", cloudtrail);
    assertThat(adapter().isArray(result), is(true));
    assertThat(adapter().toList(result), is(empty()));
  }

  @Test
  public void selectionStartsProjection() {
    T result = evaluate("Records[?@].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void selectionTestReferencingProperty() {
    T result = evaluate("Records[*].responseElements | [?keyFingerprint]", cloudtrail);
    List<T> elements = adapter().toList(result);
    assertThat(adapter().isArray(result), is(true));
    assertThat(elements, hasSize(1));
    assertThat(adapter().getProperty(elements.get(0), "keyName"), is(jsonString("mykeypair")));
  }

  @Test
  public void selectionDoesNotSelectProjectionPutEachProjectedElement() {
    T result = evaluate("Records[*].responseElements.keyName[?@]", cloudtrail);
    assertThat(adapter().isArray(result), is(true));
    assertThat(adapter().toList(result), is(empty()));
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
    T userNames = adapter().getProperty(result, "userNames");
    T keyName = adapter().getProperty(result, "keyName");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(keyName, is(jsonString("mykeypair")));
  }

  @Test
  public void createObjectInPipe() {
    T result = evaluate("Records[*].userIdentity | {userNames: [*].userName, anyUsedMfa: ([?sessionContext.attributes.mfaAuthenticated] | !!@)}", cloudtrail);
    T userNames = adapter().getProperty(result, "userNames");
    T anyUsedMfa = adapter().getProperty(result, "anyUsedMfa");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(anyUsedMfa, is(jsonBoolean(true)));
  }

  @Test
  public void createObjectInProjection() {
    T result = evaluate("Records[*].userIdentity.{userName: userName, usedMfa: sessionContext.attributes.mfaAuthenticated}", cloudtrail);
    List<T> elements = adapter().toList(result);
    assertThat(adapter().getProperty(elements.get(0), "usedMfa"), is(jsonNull()));
    assertThat(adapter().getProperty(elements.get(1), "usedMfa"), is(jsonNull()));
    assertThat(adapter().getProperty(elements.get(2), "usedMfa"), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateObject() {
    T result = evaluate("Records[*].userIdentity | {users: {names: [*].userName}}", cloudtrail);
    T names = adapter().getProperty(adapter().getProperty(result, "users"), "names");
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
    List<T> elements = adapter().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonString("mykeypair")));
  }

  @Test
  public void createArrayInPipe() {
    T result = evaluate("Records[*].userIdentity | [[*].userName, ([?sessionContext.attributes.mfaAuthenticated] | !!@)]", cloudtrail);
    List<T> elements = adapter().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonBoolean(true)));
  }

  @Test
  public void createArrayInProjection() {
    T result = evaluate("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]", cloudtrail);
    List<T> elements = adapter().toList(result);
    assertThat(adapter().toList(elements.get(0)).get(1), is(jsonNull()));
    assertThat(adapter().toList(elements.get(1)).get(1), is(jsonNull()));
    assertThat(adapter().toList(elements.get(2)).get(1), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateArray() {
    T result = evaluate("Records[*].userIdentity | [[*].type, [[*].userName]]", cloudtrail);
    List<T> elements = adapter().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("IAMUser", "IAMUser", "IAMUser")));
    assertThat(adapter().toList(elements.get(1)).get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void createArrayOnNullProducesNull() {
    T result = evaluate("bork.[snork]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void jsonLiteralNumber() {
    T result = evaluate("`42`", adapter().parseString("{}"));
    assertThat(result, is(adapter().parseString("42")));
  }

  @Test
  public void jsonLiteralString() {
    T result = evaluate("`\"foo\"`", adapter().parseString("{}"));
    assertThat(result, is(jsonString("foo")));
  }

  @Test
  public void jsonLiteralBoolean() {
    T result = evaluate("`true`", adapter().parseString("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void jsonLiteralArray() {
    T result = evaluate("`[42, \"foo\", true]`", adapter().parseString("{}"));
    assertThat(result, is(adapter().parseString("[42, \"foo\", true]")));
  }

  @Test
  public void jsonLiteralObject() {
    T result = evaluate("`{\"n\": 42, \"s\": \"foo\", \"b\": true}`", adapter().parseString("{}"));
    assertThat(result, is(adapter().parseString("{\"n\": 42, \"s\": \"foo\", \"b\": true}")));
  }

  @Test
  public void jsonLiteralInComparison() {
    T result = evaluate("Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("72.21.198.64")));
  }

  @Test
  public void comparingJsonLiteralsWithRawContents() {
    Query query = Query.fromString(null, "Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress");
    T result = query.evaluate(adapter(), cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("72.21.198.64")));
  }

  @Test
  public void callFunction() {
    T result = evaluate("type(@)", adapter().parseString("{}"));
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
    evaluate("bork()", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void callFunctionWithTooFewArgumentsThrowsFunctionCallException() {
    evaluate("type()", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void callFunctionWithTooManyArgumentsThrowsFunctionCallException() {
    evaluate("type(@, @, @)", adapter().parseString("{}"));
  }

  @Test
  public void absReturnsTheAbsoluteValueOfANumber() {
    T result1 = evaluate("abs(`-1`)", adapter().parseString("{}"));
    T result2 = evaluate("abs(`1`)", adapter().parseString("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(1)));
  }

  @Test(expected = FunctionCallException.class)
  public void absRequiresANumberArgument() {
    evaluate("abs('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void absRequiresExactlyOneArgument() {
    evaluate("abs(`1`, `2`)", adapter().parseString("{}"));
  }

  @Test
  public void avgReturnsTheAverageOfAnArrayOfNumbers() {
    T result = evaluate("avg(`[0, 1, 2, 3.5, 4]`)", adapter().parseString("{}"));
    assertThat(result, is(jsonNumber(2.1)));
  }

  @Test(expected = FunctionCallException.class)
  public void avgRequiresAnArrayOfNumbers() {
    evaluate("avg('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void avgRequiresExactlyOneArgument() {
    evaluate("avg(`[]`, `[]`)", adapter().parseString("{}"));
  }

  @Test
  public void containsReturnsTrueWhenTheNeedleIsFoundInTheHaystack() {
    T result = evaluate("contains(@, `3`)", adapter().parseString("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsComparesDeeply() {
    T result = evaluate("contains(@, `[\"bar\", {\"baz\": 42}]`)", adapter().parseString("[1, 2, 3, \"foo\", [\"bar\", {\"baz\": 42}]]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsReturnsFalseWhenTheNeedleIsNotFoundInTheHaystack() {
    T result = evaluate("contains(@, `4`)", adapter().parseString("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void containsSearchesInStrings() {
    T result = evaluate("contains('hello', 'hell')", adapter().parseString("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test(expected = FunctionCallException.class)
  public void containsRequiresTwoArguments() {
    evaluate("contains(@)", adapter().parseString("[]"));
  }

  @Test(expected = FunctionCallException.class)
  public void containsRequiresAnArrayOrStringAsFirstArgument() {
    evaluate("contains(@, 'foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void containsRequiresTwoArguments1() {
    evaluate("contains('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void containsRequiresTwoArguments2() {
    evaluate("contains('foo', 'bar', 'baz')", adapter().parseString("{}"));
  }

  @Test
  public void ceilReturnsTheNextWholeNumber() {
    T result1 = evaluate("ceil(`0.9`)", adapter().parseString("{}"));
    T result2 = evaluate("ceil(`33.3`)", adapter().parseString("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(34)));
  }

  @Test(expected = FunctionCallException.class)
  public void ceilRequiresANumberArgument() {
    evaluate("ceil('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void ceilRequiresExactlyOneArgument() {
    evaluate("ceil(`1`, `2`)", adapter().parseString("{}"));
  }

  @Test
  public void endsWithReturnsTrueWhenTheFirstArgumentEndsWithTheSecond() {
    T result = evaluate("ends_with(@, 'rld')", adapter().parseString("\"world\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void endsWithReturnsFalseWhenTheFirstArgumentDoesNotEndWithTheSecond() {
    T result = evaluate("ends_with(@, 'rld')", adapter().parseString("\"hello\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test(expected = FunctionCallException.class)
  public void endsWithRequiresTwoArguments() {
    evaluate("ends_with(@)", adapter().parseString("[]"));
  }

  @Test(expected = FunctionCallException.class)
  public void endsWithRequiresAStringAsFirstArgument() {
    evaluate("ends_with(@, 'foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void endsWithRequiresAStringAsSecondArgument() {
    evaluate("ends_with('foo', @)", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void endsWithRequiresTwoArguments1() {
    evaluate("ends_with('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void endsWithRequiresTwoArguments2() {
    evaluate("ends_with('foo', @, @)", adapter().parseString("{}"));
  }

  @Test
  public void floorReturnsThePreviousWholeNumber() {
    T result1 = evaluate("floor(`0.9`)", adapter().parseString("{}"));
    T result2 = evaluate("floor(`33.3`)", adapter().parseString("{}"));
    assertThat(result1, is(jsonNumber(0)));
    assertThat(result2, is(jsonNumber(33)));
  }

  @Test(expected = FunctionCallException.class)
  public void floorRequiresANumberArgument() {
    evaluate("floor('foo')", adapter().parseString("{}"));
  }

  @Test(expected = FunctionCallException.class)
  public void floorRequiresExactlyOneArgument() {
    evaluate("floor(`1`, `2`)", adapter().parseString("{}"));
  }

  @Test
  public void joinSmashesAnArrayOfStringsTogether() {
    T result = evaluate("join('|', @)", adapter().parseString("[\"foo\", \"bar\", \"baz\"]"));
    assertThat(result, is(jsonString("foo|bar|baz")));
  }

  @Test(expected = FunctionCallException.class)
  public void joinRequiresAStringAsFirstArgument() {
    evaluate("join(`3`, @)", adapter().parseString("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = FunctionCallException.class)
  public void joinRequiresAStringArrayAsSecondArgument() {
    evaluate("join('|', @)", adapter().parseString("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = FunctionCallException.class)
  public void joinRequiresTwoArguments1() {
    evaluate("join('|')", adapter().parseString("[]"));
  }

  @Test(expected = FunctionCallException.class)
  public void joinRequiresTwoArguments2() {
    evaluate("join('|', @, @)", adapter().parseString("[]"));
  }

  @Test
  public void joinWithAnEmptyArrayReturnsAnEmptyString() {
    T result = evaluate("join('|', @)", adapter().parseString("[]"));
    assertThat(result, is(jsonString("")));
  }

  @Test
  public void keysReturnsTheNamesOfAnObjectsProperties() {
    T result = evaluate("keys(@)", adapter().parseString("{\"foo\":3,\"bar\":4}"));
    assertThat(result, is(jsonArrayOfStrings("foo", "bar")));
  }

  @Test
  public void keysReturnsAnEmptyArrayWhenGivenAnEmptyObject() {
    T result = evaluate("keys(@)", adapter().parseString("{}"));
    assertThat(adapter().toList(result), is(empty()));
  }

  @Test(expected = FunctionCallException.class)
  public void keysRequiresAnObjectAsArgument() {
    evaluate("keys(@)", adapter().parseString("[3]"));
  }

  @Test(expected = FunctionCallException.class)
  public void keysRequiresASingleArgument() {
    evaluate("keys(@, @)", adapter().parseString("{}"));
  }
}
