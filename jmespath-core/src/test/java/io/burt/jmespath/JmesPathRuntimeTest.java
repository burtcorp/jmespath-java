package io.burt.jmespath;

import org.junit.Before;
import org.junit.Test;

import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import io.burt.jmespath.parser.ParseException;
import io.burt.jmespath.function.ArityException;
import io.burt.jmespath.function.ArgumentTypeException;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;

public abstract class JmesPathRuntimeTest<T> {
  protected T contact;
  protected T cloudtrail;

  protected abstract Adapter<T> runtime();

  protected T loadExample(String path) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(JmesPathRuntimeTest.class.getResourceAsStream(path), Charset.forName("UTF-8")))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
      }
      return parse(buffer.toString());
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Failed parsing %s", path), ioe);
    }
  }

  protected T search(String query, T input) {
    Expression<T> expression = runtime().compile(query);
    return expression.search(input);
  }

  protected T parse(String json) {
    return runtime().parseString(json);
  }

  protected Matcher<T> jsonBoolean(final boolean b) {
    return new BaseMatcher<T>() {
      @Override
      @SuppressWarnings("unchecked")
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
      @SuppressWarnings("unchecked")
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
      @SuppressWarnings("unchecked")
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
      @SuppressWarnings("unchecked")
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
      @SuppressWarnings("unchecked")
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
    T result = search("lastName", contact);
    assertThat(result, is(jsonString("Smith")));
  }

  @Test
  public void chainProperty() {
    T result = search("address.state", contact);
    assertThat(result, is(jsonString("NY")));
  }

  @Test
  public void propertyNotFound() {
    T result = search("address.country", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void nullValue() {
    T result = search("spouse", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void index() {
    T result = search("phoneNumbers[1].type", contact);
    assertThat(result, is(jsonString("office")));
  }

  @Test
  public void negativeIndex() {
    T result = search("phoneNumbers[-2].type", contact);
    assertThat(result, is(jsonString("office")));
  }

  @Test
  public void indexNotFound() {
    T result = search("phoneNumbers[3].type", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void negativeIndexNotFound() {
    T result = search("phoneNumbers[-4].type", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void indexOnNonArrayProducesNull() {
    T result = search("[0]", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void projection() {
    T result = search("phoneNumbers[*].type", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "office", "mobile")));
  }

  @Test
  public void multiStepProjection() {
    T result = search("Records[*].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void projectionFiltersNull() {
    T result = search("Records[*].requestParameters.keyName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("mykeypair")));
  }

  @Test
  public void projectionOnNonArrayProducesNull() {
    T result = search("[*]", contact);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void pipeStopsProjections() {
    T result = search("Records[*].userIdentity | [1].userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void projectionOnProjection() {
    T result = search("Records[*].responseElements.instancesSet.items[*].instanceId", cloudtrail);
    assertThat(result, is(parse("[[\"i-ebeaf9e2\"],[\"i-2e9faebe\"]]")));
  }


  @Test
  public void pipeStopsNestedProjections() {
    T result = search("Records[*].*.*.* | [2][0][0][] | [0].creationDate", cloudtrail);
    assertThat(result, is(jsonString("2014-03-06T15:15:06Z")));
  }

  @Test
  public void literalString() {
    T result = search("'hello world'", cloudtrail);
    assertThat(result, is(jsonString("hello world")));
  }

  @Test
  public void literalStringIgnoresSource() {
    T result = search("Records[*] | 'hello world'", cloudtrail);
    assertThat(result, is(jsonString("hello world")));
  }

  public void flattenStartsProjection() {
    T result = search("Records[].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void flattenArray() {
    T nestedArray = parse("[[0, 1, 2]]");
    T result = search("[]", nestedArray);
    assertThat(result, is(parse("[0, 1, 2]")));
  }

  @Test
  public void flattenNonArrayProducesNull() {
    T result = search("Records[0].userIdentity.userName[]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void flattenMultipleTimes() {
    T nestedArray = parse("[[0, 1, 2]]");
    T result = search("[][][][][][][][][][][][][]", nestedArray);
    assertThat(result, is(parse("[0, 1, 2]")));
  }

  @Test
  public void flattenInProjection() {
    T nestedArray = parse("[{\"a\":[0]},{\"a\":[1]}]");
    T result = search("[*].a[]", nestedArray);
    assertThat(result, is(parse("[0, 1]")));
  }

  @Test
  public void flattenObject() {
    T result = search("Records[0].userIdentity.*", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE", "123456789012", "Alice")));
  }

  @Test
  public void flattenObjectCreatesProjection() {
    T result = search("Records[0].responseElements.*.items[].instanceId", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("i-ebeaf9e2")));
  }

  @Test
  public void multipleFlattenObject() {
    T nestedObject = parse("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result = search("*.*", nestedObject);
    assertThat(result, is(parse("[[{\"inner\":1}],[{\"inner\":2}]]")));
  }

  @Test
  public void multipleFlattenObjectWithFollowingProjection() {
    T nestedObject = parse("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    T result1 = search("*.*.inner", nestedObject);
    assertThat(result1, is(parse("[[1],[2]]")));
    T result = search("*.*.inner[]", nestedObject);
    assertThat(result, is(parse("[1,2]")));
  }

  @Test
  public void flattenNonObjectProducesNull() {
    T result = search("Records[0].responseElements.instancesSet.items.*", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void slice() {
    T result = search("Records[0].userIdentity.* | [1::2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("EX_PRINCIPAL_ID", "EXAMPLE_KEY_ID_ALICE", "Alice")));
  }

  @Test
  public void sliceNotFound() {
    T result = search("Records[0].userIdentity.* | [99:]", cloudtrail);
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void negativeStopSlice() {
    T result = search("Records[0].userIdentity.* | [:-2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE")));
  }

  @Test
  public void negativeStartSlice() {
    T result = search("Records[0].userIdentity.* | [-3:4]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("EXAMPLE_KEY_ID_ALICE")));
  }

  @Test
  public void negativeStepSliceReversesOrder() {
    T result = search("@[::-1]", parse("[0, 1, 2, 3, 4]"));
    assertThat(result, is(parse("[4, 3, 2, 1, 0]")));
  }

  @Test
  public void negativeStepSliceReversesOrderAndSlices() {
    T result1 = search("@[3:1:-1]", parse("[0, 1, 2, 3, 4]"));
    T result2 = search("@[3:0:-1]", parse("[0, 1, 2, 3, 4]"));
    T result3 = search("@[3::-1]", parse("[0, 1, 2, 3, 4]"));
    assertThat(result1, is(parse("[3, 2]")));
    assertThat(result2, is(parse("[3, 2, 1]")));
    assertThat(result3, is(parse("[3, 2, 1, 0]")));
  }

  @Test
  public void negativeStepSliceReversesOrderAndSlicesAndHandlesOverflow() {
    T result = search("@[10:1:-1]", parse("[0, 1, 2, 3, 4]"));
    assertThat(result, is(parse("[4, 3, 2]")));
  }

  @Test
  public void negativeStepSliceReversesOrderAndSkips() {
    T result = search("Records[0].userIdentity.* | [::-2]", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "EXAMPLE_KEY_ID_ALICE", "EX_PRINCIPAL_ID")));
  }

  @Test
  public void negativeStepSliceWithOutOfBoundsNegativeStop() {
    T result = search("@[:-200:-1]", parse("[0, 1, 2, 3, 4]"));
    assertThat(result, is(parse("[4, 3, 2, 1, 0]")));
  }

  @Test
  public void sliceStartsProjection() {
    T result = search("[:2].a", parse("[{\"a\":1},{\"a\":2},{\"a\":3}]"));
    assertThat(result, is(parse("[1, 2]")));
  }

  @Test
  public void currentNodeReturnsInput() {
    T result = search("@", cloudtrail);
    assertThat(runtime().toList(runtime().getProperty(result, "Records")), hasSize(3));
  }

  @Test
  public void currentNodeAsNoOp() {
    T result = search("@ | Records[0].userIdentity | @ | userName | @ | @", cloudtrail);
    assertThat(result, is(jsonString("Alice")));
  }

  @Test
  public void andReturnsSecondOperandWhenFirstIsTruthy() {
    T result = search("Records[0].userIdentity.userName && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void andReturnsFirstOperandWhenItIsFalsy() {
    T result = search("'' && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("")));
  }

  @Test
  public void aLongChainOfAnds() {
    T result = search("@ && Records[2] && Records[2].responseElements && Records[2].responseElements.keyName", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test
  public void orReturnsFirstOperandWhenItIsTruthy() {
    T result = search("Records[0].userIdentity.userName || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Alice")));
  }

  @Test
  public void orReturnsSecondOperandWhenFirstIsFalsy() {
    T result = search("'' || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonString("Bob")));
  }

  @Test
  public void aLongChainOfOrs() {
    T result = search("'' || Records[3] || Records[2].foobar || Records[2].responseElements.keyName", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test
  public void selectionWithTrueTest() {
    T result = search("Records[?@]", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), hasSize(3));
  }

  @Test
  public void selectionWithBooleanProperty() {
    T result = search("Records[*] | [?userIdentity.sessionContext.attributes.mfaAuthenticated].eventTime", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("2014-03-06T17:10:34Z")));
  }

  @Test
  public void selectionWithFalseTest() {
    T result = search("Records[?'']", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void selectionStartsProjection() {
    T result = search("Records[?@].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void selectionTestReferencingProperty() {
    T result = search("Records[*].responseElements | [?keyFingerprint]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(elements, hasSize(1));
    assertThat(runtime().getProperty(elements.get(0), "keyName"), is(jsonString("mykeypair")));
  }

  @Test
  public void selectionDoesNotSelectProjectionPutEachProjectedElement() {
    T result = search("Records[*].responseElements.keyName[?@]", cloudtrail);
    assertThat(runtime().typeOf(result), is(JmesPathType.ARRAY));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void selectionOnNonArrayProducesNull() {
    T result = search("Records[0].userIdentity[?@]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void selectionWithComplexTest() {
    T result = search("Records[*] | [?userIdentity.userName == 'Bob' || responseElements.instancesSet.items[0].instanceId == 'i-ebeaf9e2'].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob")));
  }

  @Test
  public void compareEqualityWhenEqualProducesTrue() {
    T result = search("Records[0].userIdentity.userName == Records[2].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareEqualityWhenNotEqualProducesFalse() {
    T result = search("Records[0].userIdentity.userName == Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNonEqualityWhenEqualProducesFalse() {
    T result = search("Records[0].userIdentity.userName != Records[2].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNonEqualityWhenNotEqualProducesTrue() {
    T result = search("Records[0].userIdentity.userName != Records[1].userIdentity.userName", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersEqWhenEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code == currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersEqWhenNotEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code == previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersNotEqWhenEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code != currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersNotEqWhenNotEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code != previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGtWhenGt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code > previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGtWhenLt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState.code > currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersGteWhenGt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code >= previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGteWhenEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code >= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersGteWhenLt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState.code >= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLtWhenGt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code < previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLtWhenLt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState.code < currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersLteWhenGt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code <= previousState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void compareNumbersLteWhenEq() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | currentState.code <= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareNumbersLteWhenLt() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState.code <= currentState.code", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void compareGtWithNonNumberProducesNull() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState > currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareGteWithNonNumberProducesNull() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState >= currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareLtWithNonNumberProducesNull() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState < currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void compareLteWithNonNumberProducesNull() {
    T result = search("Records[1].responseElements.instancesSet.items[0] | previousState <= currentState", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void negateSomethingTruthyProducesFalse() {
    T result = search("!'hello'", cloudtrail);
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void negateNullProducesTrue() {
    T result = search("!Records[3]", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void negateEmptyStringProducesTrue() {
    T result = search("!''", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void negateEmptyArrayProducesTrue() {
    T result = search("Records[?''] | !@", cloudtrail);
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void createObject() {
    T result = search("{userNames: Records[*].userIdentity.userName, keyName: Records[2].responseElements.keyName}", cloudtrail);
    T userNames = runtime().getProperty(result, "userNames");
    T keyName = runtime().getProperty(result, "keyName");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(keyName, is(jsonString("mykeypair")));
  }

  @Test
  public void createObjectInPipe() {
    T result = search("Records[*].userIdentity | {userNames: [*].userName, anyUsedMfa: ([?sessionContext.attributes.mfaAuthenticated] | !!@)}", cloudtrail);
    T userNames = runtime().getProperty(result, "userNames");
    T anyUsedMfa = runtime().getProperty(result, "anyUsedMfa");
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(anyUsedMfa, is(jsonBoolean(true)));
  }

  @Test
  public void createObjectInProjection() {
    T result = search("Records[*].userIdentity.{userName: userName, usedMfa: sessionContext.attributes.mfaAuthenticated}", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().getProperty(elements.get(0), "usedMfa"), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(1), "usedMfa"), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(2), "usedMfa"), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateObject() {
    T result = search("Records[*].userIdentity | {users: {names: [*].userName}}", cloudtrail);
    T names = runtime().getProperty(runtime().getProperty(result, "users"), "names");
    assertThat(names, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void createObjectOnNullProducesNull() {
    T result = search("bork.{foo: bar}", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void createArray() {
    T result = search("[Records[*].userIdentity.userName, Records[2].responseElements.keyName]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonString("mykeypair")));
  }

  @Test
  public void createArrayInPipe() {
    T result = search("Records[*].userIdentity | [[*].userName, ([?sessionContext.attributes.mfaAuthenticated] | !!@)]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(elements.get(1), is(jsonBoolean(true)));
  }

  @Test
  public void createArrayInProjection() {
    T result = search("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().toList(elements.get(0)).get(1), is(jsonNull()));
    assertThat(runtime().toList(elements.get(1)).get(1), is(jsonNull()));
    assertThat(runtime().toList(elements.get(2)).get(1), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateArray() {
    T result = search("Records[*].userIdentity | [[*].type, [[*].userName]]", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(elements.get(0), is(jsonArrayOfStrings("IAMUser", "IAMUser", "IAMUser")));
    assertThat(runtime().toList(elements.get(1)).get(0), is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void createArrayOnNullProducesNull() {
    T result = search("bork.[snork]", cloudtrail);
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void jsonLiteralNumber() {
    T result = search("`42`", parse("{}"));
    assertThat(result, is(parse("42")));
  }

  @Test
  public void jsonLiteralString() {
    T result = search("`\"foo\"`", parse("{}"));
    assertThat(result, is(jsonString("foo")));
  }

  @Test
  public void jsonLiteralStringWithEscapedBacktick() {
    T result = search("`\"fo\\`o\"`", parse("{}"));
    assertThat(result, is(jsonString("fo`o")));
  }

  @Test
  public void jsonLiteralBoolean() {
    T result = search("`true`", parse("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void jsonLiteralArray() {
    T result = search("`[42, \"foo\", true]`", parse("{}"));
    assertThat(result, is(parse("[42, \"foo\", true]")));
  }

  @Test
  public void jsonLiteralObject() {
    T result = search("`{\"n\": 42, \"s\": \"foo\", \"b\": true}`", parse("{}"));
    assertThat(result, is(parse("{\"n\": 42, \"s\": \"foo\", \"b\": true}")));
  }

  @Test
  public void jsonLiteralInComparison() {
    T result = search("Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("72.21.198.64")));
  }

  @Test
  public void numbersAreTruthy() {
    T result = search("!@", parse("1"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void stringsAreTruthy() {
    T result = search("!@", parse("\"foo\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void nonEmptyArraysAreTruthy() {
    T result = search("!@", parse("[\"foo\"]"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void nonEmptyObjectsAreTruthy() {
    T result = search("!@", parse("{\"foo\":3}"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void trueIsTruthy() {
    T result = search("!@", parse("true"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void falseIsNotTruthy() {
    T result = search("!@", parse("false"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void nullIsNotTruthy() {
    T result = search("!@", parse("null"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyStringIsNotTruthy() {
    T result = search("!@", parse("\"\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyArrayIsNotTruthy() {
    T result = search("!@", parse("[]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void anEmptyObjectIsNotTruthy() {
    T result = search("!@", parse("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void callFunction() {
    T result = search("type(@)", parse("{}"));
    assertThat(result, is(jsonString("object")));
  }

  @Test
  public void callFunctionWithExpressionReference() {
    T result = search("map(&userIdentity.userName, Records)", cloudtrail);
    assertThat(result, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
  }

  @Test
  public void callVariadicFunction() {
    T result = search("not_null(Records[0].requestParameters.keyName, Records[1].requestParameters.keyName, Records[2].requestParameters.keyName)", cloudtrail);
    assertThat(result, is(jsonString("mykeypair")));
  }

  @Test(expected = ParseException.class)
  public void callNonExistentFunctionThrowsParseException() {
    search("bork()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void callFunctionWithTooFewArgumentsThrowsArityException() {
    search("type()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void callFunctionWithTooManyArgumentsThrowsArityException() {
    search("type(@, @, @)", parse("{}"));
  }

  @Test
  public void absReturnsTheAbsoluteValueOfANumber() {
    T result1 = search("abs(`-1`)", parse("{}"));
    T result2 = search("abs(`1`)", parse("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(1)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void absRequiresANumberArgument() {
    search("abs('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void absRequiresExactlyOneArgument() {
    search("abs(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void absRequiresAValue() {
    search("abs(&foo)", parse("{}"));
  }

  @Test
  public void avgReturnsTheAverageOfAnArrayOfNumbers() {
    T result = search("avg(`[0, 1, 2, 3.5, 4]`)", parse("{}"));
    assertThat(result, is(jsonNumber(2.1)));
  }

  @Test
  public void avgReturnsNullWhenGivenAnEmptyArray() {
    T result = search("avg(`[]`)", parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void avgRequiresAnArrayOfNumbers() {
    search("avg('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void avgRequiresExactlyOneArgument() {
    search("avg(`[]`, `[]`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void avgRequiresAValue() {
    search("avg(&foo)", parse("{}"));
  }

  @Test
  public void containsReturnsTrueWhenTheNeedleIsFoundInTheHaystack() {
    T result = search("contains(@, `3`)", parse("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsComparesDeeply() {
    T result = search("contains(@, `[\"bar\", {\"baz\": 42}]`)", parse("[1, 2, 3, \"foo\", [\"bar\", {\"baz\": 42}]]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void containsReturnsFalseWhenTheNeedleIsNotFoundInTheHaystack() {
    T result = search("contains(@, `4`)", parse("[1, 2, 3, \"foo\"]"));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test
  public void containsSearchesInStrings() {
    T result = search("contains('hello', 'hell')", parse("{}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments() {
    search("contains(@)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresAnArrayOrStringAsFirstArgument() {
    search("contains(@, 'foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments1() {
    search("contains('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void containsRequiresTwoArguments2() {
    search("contains('foo', 'bar', 'baz')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresValues1() {
    search("contains(@, &foo)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void containsRequiresValues2() {
    search("contains(&foo, 'bar')", parse("{}"));
  }

  @Test
  public void ceilReturnsTheNextWholeNumber() {
    T result1 = search("ceil(`0.9`)", parse("{}"));
    T result2 = search("ceil(`33.3`)", parse("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(34)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void ceilRequiresANumberArgument() {
    search("ceil('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void ceilRequiresExactlyOneArgument() {
    search("ceil(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void ceilRequiresAValue() {
    search("ceil(&foo)", parse("{}"));
  }

  @Test
  public void endsWithReturnsTrueWhenTheFirstArgumentEndsWithTheSecond() {
    T result = search("ends_with(@, 'rld')", parse("\"world\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void endsWithReturnsFalseWhenTheFirstArgumentDoesNotEndWithTheSecond() {
    T result = search("ends_with(@, 'rld')", parse("\"hello\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments() {
    search("ends_with('')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAStringAsFirstArgument() {
    search("ends_with(@, 'foo')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAStringAsSecondArgument() {
    search("ends_with('foo', @)", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments1() {
    search("ends_with('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void endsWithRequiresTwoArguments2() {
    search("ends_with('foo', 'bar', @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAValue1() {
    search("ends_with(&foo, 'bar')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void endsWithRequiresAValue2() {
    search("ends_with('foo', &bar)", parse("{}"));
  }

  @Test
  public void floorReturnsThePreviousWholeNumber() {
    T result1 = search("floor(`0.9`)", parse("{}"));
    T result2 = search("floor(`33.3`)", parse("{}"));
    assertThat(result1, is(jsonNumber(0)));
    assertThat(result2, is(jsonNumber(33)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void floorRequiresANumberArgument() {
    search("floor('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void floorRequiresExactlyOneArgument() {
    search("floor(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void floorRequiresAValue() {
    search("floor(&foo)", parse("{}"));
  }

  @Test
  public void joinSmashesAnArrayOfStringsTogether() {
    T result = search("join('|', @)", parse("[\"foo\", \"bar\", \"baz\"]"));
    assertThat(result, is(jsonString("foo|bar|baz")));
  }

  @Test
  public void joinHandlesDuplicates() {
    T string = runtime().createString("foo");
    T value = runtime().createArray(Arrays.asList(string, string, string));
    T result = search("join('|', @)", value);
    assertThat(result, is(jsonString("foo|foo|foo")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAStringAsFirstArgument() {
    search("join(`3`, @)", parse("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAStringArrayAsSecondArgument() {
    search("join('|', @)", parse("[\"foo\", 3, \"bar\", \"baz\"]"));
  }

  @Test(expected = ArityException.class)
  public void joinRequiresTwoArguments1() {
    search("join('|')", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void joinRequiresTwoArguments2() {
    search("join('|', @, @)", parse("[]"));
  }

  @Test
  public void joinWithAnEmptyArrayReturnsAnEmptyString() {
    T result = search("join('|', @)", parse("[]"));
    assertThat(result, is(jsonString("")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAValue1() {
    search("join(&foo, @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void joinRequiresAValue2() {
    search("join('foo', &bar)", parse("{}"));
  }

  @Test
  public void keysReturnsTheNamesOfAnObjectsProperties() {
    T result = search("keys(@)", parse("{\"foo\":3,\"bar\":4}"));
    assertThat(result, is(jsonArrayOfStrings("foo", "bar")));
  }

  @Test
  public void keysReturnsAnEmptyArrayWhenGivenAnEmptyObject() {
    T result = search("keys(@)", parse("{}"));
    assertThat(runtime().toList(result), is(empty()));
    assertThat(result, is(parse("[]")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void keysRequiresAnObjectAsArgument() {
    search("keys(@)", parse("[3]"));
  }

  @Test(expected = ArityException.class)
  public void keysRequiresASingleArgument() {
    search("keys(@, @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void keysRequiresAValue() {
    search("keys(&foo)", parse("{}"));
  }

  @Test
  public void keysCanBeUsedInComparisons() {
    T result = search("keys(@) == `[\"foo\",\"bar\"]`", parse("{\"foo\":3,\"bar\":2}"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void lengthReturnsTheLengthOfAString() {
    T result = search("length(foo)", parse("{\"foo\":\"bar\"}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void lengthReturnsTheSizeOfAnArray() {
    T result = search("length(foo)", parse("{\"foo\":[0, 1, 2, 3]}"));
    assertThat(result, is(jsonNumber(4)));
  }

  @Test
  public void lengthReturnsTheSizeOfAnObject() {
    T result = search("length(@)", parse("{\"foo\":[0, 1, 2, 3]}"));
    assertThat(result, is(jsonNumber(1)));
  }

  @Test
  public void lengthCanBeUsedInComparisons() {
    T result = search("length(@) == `3`", parse("[0, 1, 2]"));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void lengthRequiresAStringArrayOrObjectAsArgument() {
    search("length(@)", parse("3"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void lengthRequiresAValue() {
    search("length(&foo)", parse("{}"));
  }

  @Test
  public void mapTransformsAnArrayIntoAnAnotherArrayByApplyingAnExpressionToEachElement() {
    T result = search("map(&type, phoneNumbers)", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "office", "mobile")));
  }

  @Test
  public void mapReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = search("map(&foo, @)", parse("[]"));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test
  public void mapAcceptsAnArrayOfObjects() {
    T result = search("map(&a, @)", parse("[{\"a\":1},{\"a\":2}]"));
    assertThat(result, is(parse("[1,2]")));
  }

  @Test
  public void mapAcceptsAnArrayOfArrays() {
    T result = search("map(&[], @)", parse("[[1, 2, 3, [4]], [5, 6, 7, [8, 9]]]"));
    assertThat(result, is(parse("[[1, 2, 3, 4], [5, 6, 7, 8, 9]]")));
  }

  @Test
  public void mapAcceptsAnArrayOfNumbers() {
    T result = search("map(&to_string(@), @)", parse("[1, -2, 3]"));
    assertThat(result, is(parse("[\"1\", \"-2\", \"3\"]")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnExpressionAsFirstArgument() {
    search("map(@, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnArrayAsSecondArgument1() {
    search("map(&foo, @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mapRequiresAnArrayAsSecondArgument2() {
    search("map(@, &foo)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void mapRequiresTwoArguments1() {
    search("map(&foo.bar)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void mapRequiresTwoArguments2() {
    search("map(&foo.bar, @, @)", parse("[]"));
  }

  @Test
  public void maxReturnsTheGreatestOfAnArrayOfNumbers() {
    T result = search("max(`[0, 1, 4, 3.5, 2]`)", parse("{}"));
    assertThat(result, is(jsonNumber(4)));
  }

  @Test
  public void maxReturnsTheGreatestOfAnArrayOfStrings() {
    T result = search("max(`[\"a\", \"d\", \"b\"]`)", parse("{}"));
    assertThat(result, is(jsonString("d")));
  }

  @Test
  public void maxReturnsNullWhenGivenAnEmptyArray() {
    T result = search("max(`[]`)", parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresAnArrayOfNumbersOrStrings() {
    search("max('foo')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresTheElementsToBeOfTheSameType() {
    search("max(`[\"foo\", 1]`)", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void maxRequiresExactlyOneArgument() {
    search("max(`[]`, `[]`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxRequiresAValue() {
    search("max(&foo)", parse("{}"));
  }

  @Test
  public void maxByReturnsTheElementWithTheGreatestValueForAnExpressionThatReturnsStrings() {
    T result = search("max_by(phoneNumbers, &type)", contact);
    assertThat(result, is(parse("{\"type\": \"office\", \"number\": \"646 555-4567\"}")));
  }

  @Test
  public void maxByReturnsTheElementWithTheGreatestValueForAnExpressionThatReturnsNumbers() {
    T result = search("max_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": 6}, {\"foo\": 1}]"));
    assertThat(result, is(parse("{\"foo\": 6}")));
  }

  @Test
  public void maxByReturnsWithAnEmptyArrayReturnsNull() {
    T result = search("max_by(@, &foo)", parse("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByDoesNotAcceptMixedResults() {
    search("max_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByDoesNotAcceptNonStringsOrNumbers() {
    search("max_by(@, &foo)", parse("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnArrayAsFirstArgument1() {
    search("max_by(@, &foo)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnArrayAsFirstArgument2() {
    search("max_by(&foo, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void maxByRequiresAnExpressionAsSecondArgument() {
    search("max_by(@, @)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void maxByRequiresTwoArguments1() {
    search("max_by(@)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void maxByRequiresTwoArguments2() {
    search("max_by(@, &foo, @)", parse("[]"));
  }

  @Test
  public void mergeMergesObjects() {
    T result = search("merge(foo, bar)", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 2}")));
  }

  @Test
  public void mergeReturnsTheArgumentWhenOnlyGivenOne() {
    T result = search("merge(foo)", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 1}}")));
  }

  @Test
  public void mergeDoesNotMutate() {
    T result = search("merge(foo, bar) && foo", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 1}")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresObjectArguments1() {
    search("merge('foo', 'bar')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresObjectArguments2() {
    search("merge(`{}`, @)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void mergeRequiresAtLeastOneArgument() {
    search("merge()", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void mergeRequiresAValue() {
    search("merge(&foo)", parse("{}"));
  }

  @Test
  public void minReturnsTheGreatestOfAnArrayOfNumbers() {
    T result = search("min(`[0, 1, -4, 3.5, 2]`)", parse("{}"));
    assertThat(result, is(jsonNumber(-4)));
  }

  @Test
  public void minReturnsTheGreatestOfAnArrayOfStrings() {
    T result = search("min(`[\"foo\", \"bar\"]`)", parse("{}"));
    assertThat(result, is(jsonString("bar")));
  }

  @Test
  public void minReturnsNullWhenGivenAnEmptyArray() {
    T result = search("min(`[]`)", parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresAnArrayOfNumbersOrStrings() {
    search("min('foo')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresTheElementsToBeOfTheSameType() {
    search("min(`[\"foo\", 1]`)", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void minRequiresExactlyOneArgument() {
    search("min(`[]`, `[]`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minRequiresAValue() {
    search("min(&foo)", parse("{}"));
  }

  @Test
  public void minByReturnsTheElementWithTheLeastValueForAnExpressionThatReturnsStrings() {
    T result = search("min_by(phoneNumbers, &type)", contact);
    assertThat(result, is(parse("{\"type\": \"home\",\"number\": \"212 555-1234\"}")));
  }

  @Test
  public void minByReturnsTheElementWithTheLeastValueForAnExpressionThatReturnsNumbers() {
    T result = search("min_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(parse("{\"foo\": -6}")));
  }

  @Test
  public void minByReturnsWithAnEmptyArrayReturnsNull() {
    T result = search("min_by(@, &foo)", parse("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByDoesNotAcceptMixedResults() {
    search("min_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByDoesNotAcceptNonStringsOrNumbers() {
    search("min_by(@, &foo)", parse("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnArrayAsFirstArgument1() {
    search("min_by(@, &foo)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnArrayAsFirstArgument2() {
    search("min_by(&foo, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void minByRequiresAnExpressionAsSecondArgument() {
    search("min_by(@, @)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void minByRequiresTwoArguments1() {
    search("min_by(@)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void minByRequiresTwoArguments2() {
    search("min_by(@, &foo, @)", parse("[]"));
  }

  @Test
  public void notNullReturnsTheFirstNonNullArgument() {
    T result = search("not_null(`null`, `null`, `3`, `null`)", parse("{}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void notNullReturnsNullWhenGivenOnlyNull() {
    T result = search("not_null(`null`, `null`)", parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArityException.class)
  public void notNullRequiresAtLeastOneArgument() {
    search("not_null()", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void notNullRequiresAValue() {
    search("not_null(`null`, &foo)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void notNullRequiresAValueForArgumentsThatAreNotInspected() {
    search("not_null('foo', &foo)", parse("{}"));
  }

  @Test
  public void reverseReversesAnArray() {
    T result = search("reverse(@)", parse("[\"foo\", 3, 2, 1]"));
    assertThat(result, is(parse("[1, 2, 3, \"foo\"]")));
  }

  @Test
  public void reverseReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = search("reverse(@)", parse("[]"));
    assertThat(result, is(parse("[]")));
  }

  @Test
  public void reverseReversesAString() {
    T result = search("reverse('hello world')", parse("{}"));
    assertThat(result, is(jsonString("dlrow olleh")));
  }

  @Test
  public void reverseReturnsAnEmptyStringWhenGivenAnEmptyString() {
    T result = search("reverse('')", parse("{}"));
    assertThat(result, is(jsonString("")));
  }

  @Test(expected = ArityException.class)
  public void reverseRequiresOneArgument1() {
    search("reverse()", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void reverseRequiresOneArgument2() {
    search("reverse(@, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void reverseRequiresAnArrayAsArgument() {
    search("reverse(@)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void reverseRequiresAValue() {
    search("reverse(&foo)", parse("{}"));
  }

  @Test
  public void sortsSortsAnArrayOfNumbers() {
    T result = search("sort(@)", parse("[6, 7, 1]"));
    assertThat(result, is(parse("[1, 6, 7]")));
  }

  @Test
  public void sortsHandlesDuplicates() {
    T result = search("sort(@)", parse("[6, 6, 7, 1, 1]"));
    assertThat(result, is(parse("[1, 1, 6, 6, 7]")));
  }

  @Test
  public void sortsSortsAnArrayOfStrings() {
    T result = search("sort(@)", parse("[\"b\", \"a\", \"x\"]"));
    assertThat(result, is(parse("[\"a\", \"b\", \"x\"]")));
  }

  @Test
  public void sortReturnsAnEmptyArrayWhenGivenAnEmptyArray() {
    T result = search("sort(@)", parse("[]"));
    assertThat(result, is(parse("[]")));
  }

  @Test(expected = ArityException.class)
  public void sortRequiresOneArgument1() {
    search("sort()", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortRequiresOneArgument2() {
    search("sort(@, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortRequiresAnArrayAsArgument() {
    search("sort(@)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortDoesNotAcceptMixedInputs() {
    search("sort(@)", parse("[1, \"foo\"]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortRequiresAValue() {
    search("sort(&foo)", parse("{}"));
  }

  @Test
  public void sortBySortsTheInputBasedOnStringsReturnedByAnExpression() {
    T result = search("sort_by(phoneNumbers, &type)[*].type", contact);
    assertThat(result, is(jsonArrayOfStrings("home", "mobile", "office")));
  }

  @Test
  public void sortBySortsTheInputBasedOnNumbersReturnedByAnExpression() {
    T result = search("sort_by(@, &foo)[*].foo", parse("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(parse("[-6, 1, 3]")));
  }

  @Test
  public void sortByHandlesDuplicates() {
    T result = search("sort_by(@, &foo)[*].foo", parse("[{\"foo\": 3}, {\"foo\": -6}, {\"foo\": -6}, {\"foo\": 1}]"));
    assertThat(result, is(parse("[-6, -6, 1, 3]")));
  }

  @Test
  public void sortBySortsIsStable() {
    T result = search("sort_by(@, &foo)[*].x", parse("[{\"foo\": 3, \"x\": 3}, {\"foo\": 3, \"x\": 1}, {\"foo\": 1}]"));
    assertThat(result, is(parse("[3, 1]")));
  }

  @Test
  public void sortByReturnsWithAnEmptyArrayReturnsNull() {
    T result = search("sort_by(@, &foo)", parse("[]"));
    assertThat(result, is(parse("[]")));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByDoesNotAcceptMixedResults() {
    search("sort_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByDoesNotAcceptNonStringsOrNumbers() {
    search("sort_by(@, &foo)", parse("[{\"foo\": []}]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnArrayAsFirstArgument1() {
    search("sort_by(@, &foo)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnArrayAsFirstArgument2() {
    search("sort_by(&foo, @)", parse("[]"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sortByRequiresAnExpressionAsSecondArgument() {
    search("sort_by(@, @)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortByRequiresTwoArguments1() {
    search("sort_by(@)", parse("[]"));
  }

  @Test(expected = ArityException.class)
  public void sortByRequiresTwoArguments2() {
    search("sort_by(@, &foo, @)", parse("[]"));
  }

  @Test
  public void startsWithReturnsTrueWhenTheFirstArgumentEndsWithTheSecond() {
    T result = search("starts_with(@, 'wor')", parse("\"world\""));
    assertThat(result, is(jsonBoolean(true)));
  }

  @Test
  public void startsWithReturnsFalseWhenTheFirstArgumentDoesNotEndWithTheSecond() {
    T result = search("starts_with(@, 'wor')", parse("\"hello\""));
    assertThat(result, is(jsonBoolean(false)));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments() {
    search("starts_with('')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAStringAsFirstArgument() {
    search("starts_with(@, 'foo')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAStringAsSecondArgument() {
    search("starts_with('foo', @)", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments1() {
    search("starts_with('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void startsWithRequiresTwoArguments2() {
    search("starts_with('foo', 'bar', @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAValue1() {
    search("starts_with(&foo, 'bar')", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void startsWithRequiresAValue2() {
    search("starts_with('foo', &bar)", parse("{}"));
  }

  @Test
  public void sumReturnsTheAverageOfAnArrayOfNumbers() {
    T result = search("sum(`[0, 1, 2, 3.5, 4]`)", parse("{}"));
    assertThat(result, is(jsonNumber(10.5)));
  }

  @Test
  public void sumReturnsZeroWhenGivenAnEmptyArray() {
    T result = search("sum(`[]`)", parse("{}"));
    assertThat(result, is(jsonNumber(0)));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sumRequiresAnArrayOfNumbers() {
    search("sum('foo')", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void sumRequiresExactlyOneArgument() {
    search("sum(`[]`, `[]`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void sumRequiresAValue() {
    search("sum(&foo)", parse("{}"));
  }

  @Test
  public void toArrayReturnsASingletonArrayWithTheArgument() {
    T result = search("to_array(`34`)", parse("{}"));
    assertThat(result, is(parse("[34]")));
  }

  @Test
  public void toArrayWithAnArrayReturnsTheArgument() {
    T result = search("to_array(@)", parse("[0, 1, 2, 3.5, 4]"));
    assertThat(result, is(parse("[0, 1, 2, 3.5, 4]")));
  }

  @Test(expected = ArityException.class)
  public void toArrayRequiresExactlyOneArgument1() {
    search("to_array()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void toArrayRequiresExactlyOneArgument2() {
    search("to_array(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toArrayRequiresAValue() {
    search("to_array(&foo)", parse("{}"));
  }

  @Test
  public void toStringReturnsTheJsonEncodingOfTheArgument() {
    T input = parse("{\"foo\": [1, 2, [\"bar\"]]}");
    T result = search("to_string(@)", input);
    assertThat(runtime().toString(result), both(containsString("\"foo\"")).and(is(runtime().toString(input))));
  }

  @Test
  public void toStringReturnsTheJsonEncodingOfNull() {
    T result = search("to_string(`null`)", parse("{}"));
    assertThat(runtime().toString(result), is("null"));
  }

  @Test
  public void toStringEncodesNewlinesTabsEtc1() {
    T result = search("to_string(@)", runtime().createArray(Arrays.asList(runtime().createString("\"Hello\"\nwo\r\\ld\t"))));
    assertThat(runtime().toString(result), is("[\"\\\"Hello\\\"\\nwo\\r\\\\ld\\t\"]"));
  }

  @Test
  public void toStringEncodesNewlinesTabsEtc2() {
    T result = search("to_string(@)", runtime().createObject(Collections.singletonMap(runtime().createString("\"Hello\"\nwo\r\\ld\t"), runtime().createString("\n\r"))));
    assertThat(runtime().toString(result), is("{\"\\\"Hello\\\"\\nwo\\r\\\\ld\\t\":\"\\n\\r\"}"));
  }

  @Test
  public void toStringWithAStringReturnsTheArgument() {
    T result = search("to_string('hello')", parse("{}"));
    assertThat(result, is(jsonString("hello")));
  }

  @Test(expected = ArityException.class)
  public void toStringRequiresExactlyOneArgument1() {
    search("to_string()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void toStringRequiresExactlyOneArgument2() {
    search("to_string(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toStringRequiresAValue() {
    search("to_string(&foo)", parse("{}"));
  }

  @Test
  public void toNumberWithANumberReturnsTheArgument() {
    T result = search("to_number(`3`)", parse("{}"));
    assertThat(result, is(jsonNumber(3)));
  }

  @Test
  public void toNumberParsesAnIntegerStringToANumber() {
    T result = search("to_number('33')", parse("{}"));
    assertThat(result, is(jsonNumber(33)));
  }

  @Test
  public void toNumberParsesAnFloatStringToANumber() {
    T result = search("to_number('3.3')", parse("{}"));
    assertThat(result, is(jsonNumber(3.3)));
  }

  @Test
  public void toNumberReturnsNullWhenGivenNonNumberString() {
    T result = search("to_number('n=3.3')", parse("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenAnArray() {
    T result = search("to_number(@)", parse("[]"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenAnObject() {
    T result = search("to_number(@)", parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenABoolean() {
    T result = search("to_number(@)", parse("true"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void toNumberReturnsNullWhenGivenNull() {
    T result = search("to_number(@)", parse("null"));
    assertThat(result, is(jsonNull()));
  }

  @Test(expected = ArityException.class)
  public void toNumberRequiresExactlyOneArgument1() {
    search("to_number()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void toNumberRequiresExactlyOneArgument2() {
    search("to_number(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void toNumberRequiresAValue() {
    search("to_number(&foo)", parse("{}"));
  }

  @Test
  public void typeReturnsTheTypeOfTheArgument() {
    assertThat(search("type(@)", parse("null")), is(jsonString("null")));
    assertThat(search("type(@)", parse("false")), is(jsonString("boolean")));
    assertThat(search("type(@)", parse("{\"foo\":3}")), is(jsonString("object")));
    assertThat(search("type(@)", parse("[3, 4]")), is(jsonString("array")));
    assertThat(search("type(@)", parse("\"foo\"")), is(jsonString("string")));
    assertThat(search("type(@)", parse("1")), is(jsonString("number")));
  }

  @Test(expected = ArityException.class)
  public void typeRequiresExactlyOneArgument1() {
    search("type()", parse("{}"));
  }

  @Test(expected = ArityException.class)
  public void typeRequiresExactlyOneArgument2() {
    search("type(`1`, `2`)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void typeRequiresAValue() {
    search("type(&foo)", parse("{}"));
  }

  @Test
  public void valuesReturnsTheValuesOfAnObjectsProperties() {
    T result = search("values(@)", parse("{\"foo\":\"one\",\"bar\":\"two\"}"));
    assertThat(result, is(jsonArrayOfStrings("one", "two")));
  }

  @Test
  public void valuesReturnsAnEmptyArrayWhenGivenAnEmptyObject() {
    T result = search("values(@)", parse("{}"));
    assertThat(runtime().toList(result), is(empty()));
  }

  @Test(expected = ArgumentTypeException.class)
  public void valuesRequiresAnObjectAsArgument() {
    search("values(@)", parse("[3]"));
  }

  @Test(expected = ArityException.class)
  public void valuesRequiresASingleArgument() {
    search("values(@, @)", parse("{}"));
  }

  @Test(expected = ArgumentTypeException.class)
  public void valuesRequiresAValue() {
    search("values(&foo)", parse("{}"));
  }

  @Test
  public void valuesFromTheInputAreEqualToValuesFromLiterals() {
    T input = parse("{\"a\":1,\"b\":2.0,\"c\":\"foo\"}");
    assertThat(search("a == `1`", input), is(jsonBoolean(true)));
    assertThat(search("b == `2.0`", input), is(jsonBoolean(true)));
    assertThat(search("c == `\"foo\"`", input), is(jsonBoolean(true)));
    assertThat(search("c == 'foo'", input), is(jsonBoolean(true)));
  }

  @Test
  public void calculatedValuesAreEqualToValuesFromLiterals() {
    T input = parse("{\"a\":[1],\"b\":-2.0,\"c\":[\"fo\",\"o\"]}");
    assertThat(search("length(a) == `1`", input), is(jsonBoolean(true)));
    assertThat(search("[length(a)] == `[1]`", input), is(jsonBoolean(true)));
    assertThat(search("{size: length(a)} == `{\"size\": 1}`", input), is(jsonBoolean(true)));
    assertThat(search("abs(b) == `2.0`", input), is(jsonBoolean(true)));
    assertThat(search("join('', c) == `\"foo\"`", input), is(jsonBoolean(true)));
    assertThat(search("join('', c) == 'foo'", input), is(jsonBoolean(true)));
  }
}
