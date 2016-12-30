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
import java.util.Collection;
import java.util.Collections;

import io.burt.jmespath.RuntimeConfiguration;
import io.burt.jmespath.parser.ParseException;
import io.burt.jmespath.function.ArgumentTypeException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;

public abstract class JmesPathRuntimeTest<T> {
  private Adapter<T> runtime = createRuntime(RuntimeConfiguration.defaultConfiguration());

  protected T contact;
  protected T cloudtrail;

  protected Adapter<T> runtime() { return runtime; }

  protected abstract Adapter<T> createRuntime(RuntimeConfiguration configuration);

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

  @Test
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
    assertThat(runtime().toList(runtime().getProperty(result, runtime().createString("Records"))), hasSize(3));
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
    assertThat(runtime().getProperty(elements.get(0), runtime().createString("keyName")), is(jsonString("mykeypair")));
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
    T userNames = runtime().getProperty(result, runtime().createString("userNames"));
    T keyName = runtime().getProperty(result, runtime().createString("keyName"));
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(keyName, is(jsonString("mykeypair")));
  }

  @Test
  public void createObjectInPipe() {
    T result = search("Records[*].userIdentity | {userNames: [*].userName, anyUsedMfa: ([?sessionContext.attributes.mfaAuthenticated] | !!@)}", cloudtrail);
    T userNames = runtime().getProperty(result, runtime().createString("userNames"));
    T anyUsedMfa = runtime().getProperty(result, runtime().createString("anyUsedMfa"));
    assertThat(userNames, is(jsonArrayOfStrings("Alice", "Bob", "Alice")));
    assertThat(anyUsedMfa, is(jsonBoolean(true)));
  }

  @Test
  public void createObjectInProjection() {
    T result = search("Records[*].userIdentity.{userName: userName, usedMfa: sessionContext.attributes.mfaAuthenticated}", cloudtrail);
    List<T> elements = runtime().toList(result);
    assertThat(runtime().getProperty(elements.get(0), runtime().createString("usedMfa")), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(1), runtime().createString("usedMfa")), is(jsonNull()));
    assertThat(runtime().getProperty(elements.get(2), runtime().createString("usedMfa")), is(jsonBoolean(true)));
  }

  @Test
  public void nestedCreateObject() {
    T result = search("Records[*].userIdentity | {users: {names: [*].userName}}", cloudtrail);
    T names = runtime().getProperty(runtime().getProperty(result, runtime().createString("users")), runtime().createString("names"));
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
  public void unicodeEscapesInJsonLiteralsAreUnescaped() {
    T result = search("`\"foo\\u0020bar\\u0021\"`", parse("{}"));
    assertThat(result, is(jsonString("foo bar!")));
  }

  @Test
  public void newlinesAndOtherRegularEscapesInJsonLiteralsAreUnescaped() {
    T result = search("`\"foo\\tbar\\n\"`", parse("{}"));
    assertThat(result, is(jsonString("foo\tbar\n")));
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

  @Test
  public void callingNonExistentFunctionThrowsParseException() {
    try {
      search("bork()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("unknown function \"bork\""));
    }
  }

  @Test
  public void callingAFunctionWithTooFewArgumentsIsACompileTimeError() {
    try {
      search("type()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"type\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void callingAFunctionWithTooManyArgumentsIsACompileTimeError() {
    try {
      search("type(@, @, @)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"type\" (expected 1 but was 3)"));
    }
  }

  @Test
  public void withSilentTypeErrorsTheWrongTypeOfArgumentMakesFunctionsReturnNull() {
    Adapter<T> rt = createRuntime(RuntimeConfiguration.builder().withSilentTypeErrors(true).build());
    T result = rt.compile("abs('foo')").search(parse("{}"));
    assertThat(result, is(jsonNull()));
  }

  @Test
  public void withSilentTypeErrorsTheWrongTypeOfArgumentMakesHigherOrderFunctionsReturnNull() {
    Adapter<T> rt = createRuntime(RuntimeConfiguration.builder().withSilentTypeErrors(true).build());
    T result1 = rt.compile("sort_by(@, &foo)").search(parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
    T result2 = rt.compile("min_by(@, &foo)").search(parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
    assertThat(result1, is(jsonNull()));
    assertThat(result2, is(jsonNull()));
  }

  @Test
  public void absReturnsTheAbsoluteValueOfANumber() {
    T result1 = search("abs(`-1`)", parse("{}"));
    T result2 = search("abs(`1`)", parse("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(1)));
  }

  @Test
  public void absRequiresANumberArgument() {
    try {
      search("abs('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void absRequiresExactlyOneArgument() {
    try {
      search("abs(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"abs\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void absRequiresAValue() {
    try {
      search("abs(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was expression"));
    }
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

  @Test
  public void avgRequiresAnArrayOfNumbers() {
    try {
      search("avg('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number but was string"));
    }
  }

  @Test
  public void avgRequiresExactlyOneArgument() {
    try {
      search("avg(`[]`, `[]`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"avg\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void avgRequiresAValue() {
    try {
      search("avg(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number but was expression"));
    }
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

  @Test
  public void containsRequiresAnArrayOrStringAsFirstArgument() {
    try {
      search("contains(@, 'foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array or string but was object"));
    }
  }

  @Test
  public void containsRequiresTwoArguments1() {
    try {
      search("contains('foo')", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"contains\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void containsRequiresTwoArguments2() {
    try {
      search("contains('foo', 'bar', 'baz')", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"contains\" (expected 2 but was 3)"));
    }
  }

  @Test
  public void containsRequiresValues1() {
    try {
      search("contains(@, &foo)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
  }

  @Test
  public void containsRequiresValues2() {
    try {
      search("contains(&foo, 'bar')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array or string but was expression"));
    }
  }

  @Test
  public void ceilReturnsTheNextWholeNumber() {
    T result1 = search("ceil(`0.9`)", parse("{}"));
    T result2 = search("ceil(`33.3`)", parse("{}"));
    assertThat(result1, is(jsonNumber(1)));
    assertThat(result2, is(jsonNumber(34)));
  }

  @Test
  public void ceilRequiresANumberArgument() {
    try {
      search("ceil('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void ceilRequiresExactlyOneArgument() {
    try {
      search("ceil(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"ceil\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void ceilRequiresAValue() {
    try {
      search("ceil(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was expression"));
    }
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

  @Test
  public void endsWithRequiresAStringAsFirstArgument() {
    try {
      search("ends_with(@, 'foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was object"));
    }
  }

  @Test
  public void endsWithRequiresAStringAsSecondArgument() {
    try {
      search("ends_with('foo', @)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was object"));
    }
  }

  @Test
  public void endsWithRequiresTwoArguments1() {
    try {
      search("ends_with('foo')", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"ends_with\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void endsWithRequiresTwoArguments2() {
    try {
      search("ends_with('foo', 'bar', @)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"ends_with\" (expected 2 but was 3)"));
    }
  }

  @Test
  public void endsWithRequiresAValue1() {
    try {
      search("ends_with(&foo, 'bar')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was expression"));
    }
  }

  @Test
  public void endsWithRequiresAValue2() {
    try {
      search("ends_with('foo', &bar)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was expression"));
    }
  }

  @Test
  public void floorReturnsThePreviousWholeNumber() {
    T result1 = search("floor(`0.9`)", parse("{}"));
    T result2 = search("floor(`33.3`)", parse("{}"));
    assertThat(result1, is(jsonNumber(0)));
    assertThat(result2, is(jsonNumber(33)));
  }

  @Test
  public void floorRequiresANumberArgument() {
    try {
      search("floor('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void floorRequiresExactlyOneArgument() {
    try {
      search("floor(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"floor\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void floorRequiresAValue() {
    try {
      search("floor(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was expression"));
    }
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

  @Test
  public void joinRequiresAStringAsFirstArgument() {
    try {
      search("join(`3`, @)", parse("[\"foo\", 3, \"bar\", \"baz\"]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was number"));
    }
  }

  @Test
  public void joinRequiresAStringArrayAsSecondArgument() {
    try {
      search("join('|', @)", parse("[\"foo\", 3, \"bar\", \"baz\"]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of string but was array containing string and number"));
    }
  }

  @Test
  public void joinRequiresTwoArguments1() {
    try {
      search("join('|')", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"join\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void joinRequiresTwoArguments2() {
    try {
      search("join('|', @, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"join\" (expected 2 but was 3)"));
    }
  }

  @Test
  public void joinWithAnEmptyArrayReturnsAnEmptyString() {
    T result = search("join('|', @)", parse("[]"));
    assertThat(result, is(jsonString("")));
  }

  @Test
  public void joinRequiresAValue1() {
    try {
      search("join(&foo, @)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was expression"));
    }
  }

  @Test
  public void joinRequiresAValue2() {
    try {
      search("join('foo', &bar)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of string but was expression"));
    }
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

  @Test
  public void keysRequiresAnObjectAsArgument() {
    try {
      search("keys(@)", parse("[3]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was array"));
    }
  }

  @Test
  public void keysRequiresASingleArgument() {
    try {
      search("keys(@, @)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"keys\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void keysRequiresAValue() {
    try {
      search("keys(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was expression"));
    }
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

  @Test
  public void lengthRequiresAStringArrayOrObjectAsArgument() {
    try {
      search("length(@)", parse("3"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string, array or object but was number"));
    }
  }

  @Test
  public void lengthRequiresAValue() {
    try {
      search("length(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string, array or object but was expression"));
    }
  }

  @Test
  public void lengthRequiresAnArgument() {
    try {
      search("length()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"length\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void lengthRequiresExactlyOneArgument() {
    try {
      search("length(@, @)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"length\" (expected 1 but was 2)"));
    }
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

  @Test
  public void mapRequiresAnExpressionAsFirstArgument() {
    try {
      search("map(@, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected expression but was array"));
    }
  }

  @Test
  public void mapRequiresAnArrayAsSecondArgument1() {
    try {
      search("map(&foo, @)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of any value but was object"));
    }
  }

  @Test
  public void mapRequiresAnArrayAsSecondArgument2() {
    try {
      search("map(@, &foo)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected expression but was array"));
    }
  }

  @Test
  public void mapRequiresTwoArguments1() {
    try {
      search("map(&foo.bar)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"map\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void mapRequiresTwoArguments2() {
    try {
      search("map(&foo.bar, @, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"map\" (expected 2 but was 3)"));
    }
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

  @Test
  public void maxRequiresAnArrayOfNumbersOrStrings() {
    try {
      search("max('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was string"));
    }
  }

  @Test
  public void maxRequiresTheElementsToBeOfTheSameType() {
    try {
      search("max(`[\"foo\", 1]`)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was array containing string and number"));
    }
  }

  @Test
  public void maxRequiresExactlyOneArgument() {
    try {
      search("max(`[]`, `[]`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"max\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void maxRequiresAValue() {
    try {
      search("max(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was expression"));
    }
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

  @Test
  public void maxByDoesNotAcceptExpressionsThatResultInMixedResults() {
    try {
      search("max_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void maxByDoesNotAcceptExpressionsThatResultInNonStringsOrNumbers() {
    try {
      search("max_by(@, &foo)", parse("[{\"foo\": []}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number or string but was array"));
    }
  }

  @Test
  public void maxByRequiresAnArrayAsFirstArgument1() {
    try {
      search("max_by(@, &foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was object"));
    }
  }

  @Test
  public void maxByRequiresAnArrayAsFirstArgument2() {
    try {
      search("max_by(&foo, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was expression"));
    }
  }

  @Test
  public void maxByRequiresAnExpressionAsSecondArgument() {
    try {
      search("max_by(@, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected expression but was array"));
    }
  }

  @Test
  public void maxByRequiresTwoArguments1() {
    try {
      search("max_by(@)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"max_by\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void maxByRequiresTwoArguments2() {
    try {
      search("max_by(@, &foo, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"max_by\" (expected 2 but was 3)"));
    }
  }

  @Test
  public void mergeMergesObjects() {
    T result = search("merge(foo, bar)", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 2}")));
  }

  @Test
  public void mergeReturnsTheArgumentWhenOnlyGivenOne() {
    T result = search("merge(foo)", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 1}")));
  }

  @Test
  public void mergeDoesNotMutate() {
    T result = search("merge(foo, bar) && foo", parse("{\"foo\": {\"a\": 1, \"b\": 1}, \"bar\": {\"b\": 2}}"));
    assertThat(result, is(parse("{\"a\": 1, \"b\": 1}")));
  }

  @Test
  public void mergeRequiresObjectArguments1() {
    try {
      search("merge('foo', 'bar')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was string"));
    }
  }

  @Test
  public void mergeRequiresObjectArguments2() {
    try {
      search("merge(`{}`, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was array"));
    }
  }

  @Test
  public void mergeRequiresAtLeastOneArgument() {
    try {
      search("merge()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"merge\" (expected at least 1 but was 0)"));
    }
  }

  @Test
  public void mergeRequiresAValue() {
    try {
      search("merge(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was expression"));
    }
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

  @Test
  public void minRequiresAnArrayOfNumbersOrStrings() {
    try {
      search("min('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was string"));
    }
  }

  @Test
  public void minRequiresTheElementsToBeOfTheSameType() {
    try {
      search("min(`[\"foo\", 1]`)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was array containing string and number"));
    }
  }

  @Test
  public void minRequiresExactlyOneArgument() {
    try {
      search("min(`[]`, `[]`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"min\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void minRequiresAValue() {
    try {
      search("min(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was expression"));
    }
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

  @Test
  public void minByDoesNotAcceptMixedResults() {
    try {
      search("min_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void minByDoesNotAcceptExpressionsThatResultInNonStringsOrNumbers() {
    try {
      search("min_by(@, &foo)", parse("[{\"foo\": []}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number or string but was array"));
    }
  }

  @Test
  public void minByRequiresAnArrayAsFirstArgument1() {
    try {
      search("min_by(@, &foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was object"));
    }
  }

  @Test
  public void minByRequiresAnArrayAsFirstArgument2() {
    try {
      search("min_by(&foo, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was expression"));
    }
  }

  @Test
  public void minByRequiresAnExpressionAsSecondArgument() {
    try {
      search("min_by(@, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected expression but was array"));
    }
  }

  @Test
  public void minByRequiresTwoArguments1() {
    try {
      search("min_by(@)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"min_by\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void minByRequiresTwoArguments2() {
    try {
      search("min_by(@, &foo, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"min_by\" (expected 2 but was 3)"));
    }
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

  @Test
  public void notNullRequiresAtLeastOneArgument() {
    try {
      search("not_null()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"not_null\" (expected at least 1 but was 0)"));
    }
  }

  @Test
  public void notNullRequiresAValue() {
    try {
      search("not_null(`null`, &foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
  }

  @Test
  public void notNullRequiresAValueForArgumentsThatAreNotInspected() {
    try {
      search("not_null('foo', &foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
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

  @Test
  public void reverseRequiresOneArgument1() {
    try {
      search("reverse()", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"reverse\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void reverseRequiresOneArgument2() {
    try {
      search("reverse(@, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"reverse\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void reverseRequiresAnArrayAsArgument() {
    try {
      search("reverse(@)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array or string but was object"));
    }
  }

  @Test
  public void reverseRequiresAValue() {
    try {
      search("reverse(&foo)", parse("{}"));
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array or string but was expression"));
    }
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

  @Test
  public void sortRequiresOneArgument1() {
    try {
      search("sort()", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"sort\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void sortRequiresOneArgument2() {
    try {
      search("sort(@, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"sort\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void sortRequiresAnArrayAsArgument() {
    try {
      search("sort(@)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was object"));
    }
  }

  @Test
  public void sortDoesNotAcceptMixedInputs() {
    try {
      search("sort(@)", parse("[1, \"foo\"]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was array containing number and string"));
    }
  }

  @Test
  public void sortRequiresAValue() {
    try {
      search("sort(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number or string but was expression"));
    }
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

  @Test
  public void sortByDoesNotAcceptMixedResults() {
    try {
      search("sort_by(@, &foo)", parse("[{\"foo\": 3}, {\"foo\": \"bar\"}, {\"foo\": 1}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number but was string"));
    }
  }

  @Test
  public void sortByDoesNotAcceptExpressionsThatResultInNonStringsOrNumbers() {
    try {
      search("sort_by(@, &foo)", parse("[{\"foo\": []}]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected number or string but was array"));
    }
  }

  @Test
  public void sortByRequiresAnArrayAsFirstArgument1() {
    try {
      search("sort_by(@, &foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was object"));
    }
  }

  @Test
  public void sortByRequiresAnArrayAsFirstArgument2() {
    try {
      search("sort_by(&foo, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of object but was expression"));
    }
  }

  @Test
  public void sortByRequiresAnExpressionAsSecondArgument() {
    try {
      search("sort_by(@, @)", parse("[]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected expression but was array"));
    }
  }

  @Test
  public void sortByRequiresTwoArguments1() {
    try {
      search("sort_by(@)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"sort_by\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void sortByRequiresTwoArguments2() {
    try {
      search("sort_by(@, &foo, @)", parse("[]"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"sort_by\" (expected 2 but was 3)"));
    }
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

  @Test
  public void startsWithRequiresAStringAsFirstArgument() {
    try {
      search("starts_with(@, 'foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was object"));
    }
  }

  @Test
  public void startsWithRequiresAStringAsSecondArgument() {
    try {
      search("starts_with('foo', @)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was object"));
    }
  }

  @Test
  public void startsWithRequiresTwoArguments1() {
    try {
      search("starts_with('foo')", parse("{}"));
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"starts_with\" (expected 2 but was 1)"));
    }
  }

  @Test
  public void startsWithRequiresTwoArguments2() {
    try {
      search("starts_with('foo', 'bar', @)", parse("{}"));
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"starts_with\" (expected 2 but was 3)"));
    }
  }

  @Test
  public void startsWithRequiresAValue1() {
    try {
      search("starts_with(&foo, 'bar')", parse("{}"));
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was expression"));
    }
  }

  @Test
  public void startsWithRequiresAValue2() {
    try {
      search("starts_with('foo', &bar)", parse("{}"));
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected string but was expression"));
    }
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

  @Test
  public void sumRequiresAnArrayOfNumbers() {
    try {
      search("sum('foo')", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number but was string"));
    }
  }

  @Test
  public void sumRequiresExactlyOneArgument() {
    try {
      search("sum(`[]`, `[]`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"sum\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void sumRequiresAValue() {
    try {
      search("sum(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected array of number but was expression"));
    }
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

  @Test
  public void toArrayRequiresExactlyOneArgument1() {
    try {
      search("to_array()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_array\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void toArrayRequiresExactlyOneArgument2() {
    try {
      search("to_array(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_array\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void toArrayRequiresAValue() {
    try {
      search("to_array(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
  }

  @Test
  public void toStringReturnsTheJsonEncodingOfTheArgument() {
    T input = parse("{\"foo\": [1, 2, [\"bar\"], false], \"bar\": null}");
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

  @Test
  public void toStringRequiresExactlyOneArgument1() {
    try {
      search("to_string()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_string\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void toStringRequiresExactlyOneArgument2() {
    try {
      search("to_string(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_string\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void toStringRequiresAValue() {
    try {
      search("to_string(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
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

  @Test
  public void toNumberRequiresExactlyOneArgument1() {
    try {
      search("to_number()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_number\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void toNumberRequiresExactlyOneArgument2() {
    try {
      search("to_number(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"to_number\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void toNumberRequiresAValue() {
    try {
      search("to_number(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
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

  @Test
  public void typeRequiresExactlyOneArgument1() {
    try {
      search("type()", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"type\" (expected 1 but was 0)"));
    }
  }

  @Test
  public void typeRequiresExactlyOneArgument2() {
    try {
      search("type(`1`, `2`)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"type\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void typeRequiresAValue() {
    try {
      search("type(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected any value but was expression"));
    }
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

  @Test
  public void valuesRequiresAnObjectAsArgument() {
    try {
      search("values(@)", parse("[3]"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was array"));
    }
  }

  @Test
  public void valuesRequiresASingleArgument() {
    try {
      search("values(@, @)", parse("{}"));
      fail("Expected ParseException to have been thrown");
    } catch (ParseException pe) {
      assertThat(pe.getMessage(), containsString("invalid arity calling \"values\" (expected 1 but was 2)"));
    }
  }

  @Test
  public void valuesRequiresAValue() {
    try {
      search("values(&foo)", parse("{}"));
      fail("Expected ArgumentTypeException to have been thrown");
    } catch (ArgumentTypeException ate) {
      assertThat(ate.getMessage(), containsString("expected object but was expression"));
    }
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

  @Test
  public void toListReturnsAListWhenGivenAnArray() {
    List<T> list = runtime().toList(parse("[1, 2, 3]"));
    assertThat(list, is(Arrays.asList(parse("1"), parse("2"), parse("3"))));
  }

  @Test
  public void toListReturnsTheValuesOfAnObjectInOrder() {
    List<T> list = runtime().toList(parse("{\"one\":1,\"two\":2,\"three\":3}"));
    assertThat(list, is(Arrays.asList(parse("1"), parse("2"), parse("3"))));
  }

  @Test
  public void toListReturnsAnEmptyListWhenGivenSomethingThatIsNotArrayOrObject() {
    List<T> list = runtime().toList(parse("3"));
    assertThat(list, is(empty()));
  }

  @Test
  public void toNumberReturnsANullValueWhenGivenANonNumber() {
    Number n = runtime().toNumber(parse("[]"));
    assertThat(n, is(equalTo(null)));
  }

  @Test
  public void getPropertyNamesReturnsAnEmptyListWhenGivenANonObject() {
    Collection<T> properties = runtime().getPropertyNames(parse("[]"));
    assertThat(properties, is(empty()));
  }

  @Test(expected = Exception.class)
  public void parseStringThrowsImplementationSpecificExceptionWhenGivenBadJson() {
    parse("{");
  }

  @Test
  public void compareReturnsNonZeroWhenTwoArraysAreNotEqual() {
    int result1 = runtime().compare(parse("[1]"), parse("[1,2]"));
    int result2 = runtime().compare(parse("[1,3]"), parse("[1,2]"));
    assertThat(result1, is(not(0)));
    assertThat(result2, is(not(0)));
  }

  @Test
  public void compareReturnsNonZeroWhenTwoObjectsAreNotEqual() {
    int result1 = runtime().compare(parse("{\"one\":1}"), parse("{\"one\":1,\"two\":2}"));
    int result2 = runtime().compare(parse("{\"one\":1,\"two\":2,\"three\":3}"), parse("{\"one\":1,\"two\":2}"));
    assertThat(result1, is(not(0)));
    assertThat(result2, is(not(0)));
  }
}
