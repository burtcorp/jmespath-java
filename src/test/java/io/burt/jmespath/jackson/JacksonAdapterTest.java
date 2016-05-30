package io.burt.jmespath.jackson;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.burt.jmespath.Query;
import io.burt.jmespath.function.FunctionCallException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;

public class JacksonAdapterTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  private JsonNode contact;
  private JsonNode cloudtrail;
  private JacksonAdapter adapter;

  private JsonNode loadExample(String path) {
    try {
      return objectMapper.readTree(getClass().getResource(path));
    } catch (IOException ioe) {
      fail(String.format("Failed parsing %s: \"%s\"", path, ioe.getMessage()));
      return null;
    }
  }

  private JsonNode parseString(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (IOException ioe) {
      fail(String.format("Failed parsing %s: \"%s\"", json, ioe.getMessage()));
      return null;
    }
  }

  private List<String> toStringList(JsonNode node) {
    List<String> strings = new ArrayList<>(node.size());
    for (JsonNode element : node) {
      strings.add(element.textValue());
    }
    return strings;
  }

  private JsonNode evaluate(String query, JsonNode input) {
    return Query.fromString(query).evaluate(adapter, input);
  }

  @Before
  public void beforeEach() {
    contact = loadExample("/contact.json");
    cloudtrail = loadExample("/cloudtrail.json");
    adapter = new JacksonAdapter();
  }

  @Test
  public void topLevelProperty() {
    JsonNode result = evaluate("lastName", contact);
    assertThat(result.textValue(), is("Smith"));
  }

  @Test
  public void chainProperty() {
    JsonNode result = evaluate("address.state", contact);
    assertThat(result.textValue(), is("NY"));
  }

  @Test
  public void propertyNotFound() {
    JsonNode result = evaluate("address.country", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void nullValue() {
    JsonNode result = evaluate("spouse", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void index() {
    JsonNode result = evaluate("phoneNumbers[1].type", contact);
    assertThat(result.textValue(), is("office"));
  }

  @Test
  public void negativeIndex() {
    JsonNode result = evaluate("phoneNumbers[-2].type", contact);
    assertThat(result.textValue(), is("office"));
  }

  @Test
  public void indexNotFound() {
    JsonNode result = evaluate("phoneNumbers[3].type", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void negativeIndexNotFound() {
    JsonNode result = evaluate("phoneNumbers[-4].type", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void indexOnNonArrayProducesNull() {
    JsonNode result = evaluate("[0]", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void projection() {
    JsonNode result = evaluate("phoneNumbers[*].type", contact);
    assertThat(toStringList(result), contains("home", "office", "mobile"));
  }

  @Test
  public void multiStepProjection() {
    JsonNode result = evaluate("Records[*].userIdentity.userName", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void projectionFiltersNull() {
    JsonNode result = evaluate("Records[*].requestParameters.keyName", cloudtrail);
    assertThat(toStringList(result), contains("mykeypair"));
  }

  @Test
  public void projectionOnNonArrayProducesNull() {
    JsonNode result = evaluate("[*]", contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void pipeStopsProjections() {
    JsonNode result = evaluate("Records[*].userIdentity | [1].userName", cloudtrail);
    assertThat(result.textValue(), is("Bob"));
  }

  @Test
  public void literalString() {
    JsonNode result = evaluate("'hello world'", cloudtrail);
    assertThat(result.textValue(), is("hello world"));
  }

  @Test
  public void literalStringIgnoresSource() {
    JsonNode result = evaluate("Records[*] | 'hello world'", cloudtrail);
    assertThat(result.textValue(), is("hello world"));
  }

  public void flattenStartsProjection() {
    JsonNode result = evaluate("Records[].userIdentity.userName", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void flattenArray() {
    JsonNode nestedArray = parseString("[[0, 1, 2]]");
    JsonNode result = evaluate("[]", nestedArray);
    assertThat(result, is(parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenNonArrayProducesNull() {
    JsonNode result = evaluate("Records[0].userIdentity.userName[]", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void flattenMultipleTimes() {
    JsonNode nestedArray = parseString("[[0, 1, 2]]");
    JsonNode result = evaluate("[][][][][][][][][][][][][]", nestedArray);
    assertThat(result, is(parseString("[0, 1, 2]")));
  }

  @Test
  public void flattenInProjection() {
    JsonNode nestedArray = parseString("[{\"a\":[0]},{\"a\":[1]}]");
    JsonNode result = evaluate("[*].a[]", nestedArray);
    assertThat(result, is(parseString("[0, 1]")));
  }

  @Test
  public void flattenObject() {
    JsonNode result = evaluate("Records[0].userIdentity.*", cloudtrail);
    assertThat(toStringList(result), contains("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE", "123456789012", "Alice"));
  }

  @Test
  public void flattenObjectCreatesProjection() {
    JsonNode result = evaluate("Records[0].responseElements.*.items[].instanceId", cloudtrail);
    assertThat(toStringList(result), contains("i-ebeaf9e2"));
  }

  @Test
  public void multipleFlattenObject() {
    JsonNode nestedObject = parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    JsonNode result = evaluate("*.*", nestedObject);
    assertThat(result, is(parseString("[[{\"inner\":1}],[{\"inner\":2}]]")));
  }

  @Test
  @Ignore("How can the result in the first assertion be the basis for the result in the second?")
  public void multipleFlattenObjectWithFollowingProjection() {
    JsonNode nestedObject = parseString("{\"a\":{\"aa\":{\"inner\":1}},\"b\":{\"bb\":{\"inner\":2}}}");
    JsonNode result1 = evaluate("*.*.inner", nestedObject);
    assertThat(result1, is(parseString("[[1],[2]]")));
    JsonNode result = evaluate("*.*.inner[]", nestedObject);
    assertThat(result, is(parseString("[1,2]")));
  }

  @Test
  public void flattenNonObjectProducesNull() {
    JsonNode result = evaluate("Records[0].responseElements.instancesSet.items.*", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void slice() {
    JsonNode result = evaluate("Records[0].userIdentity.* | [1::2]", cloudtrail);
    assertThat(toStringList(result), contains("EX_PRINCIPAL_ID", "EXAMPLE_KEY_ID_ALICE", "Alice"));
  }

  @Test
  public void sliceNotFound() {
    JsonNode result = evaluate("Records[0].userIdentity.* | [99:]", cloudtrail);
    assertThat(result.size(), is(0));
  }

  @Test
  public void negativeStopSlice() {
    JsonNode result = evaluate("Records[0].userIdentity.* | [:-2]", cloudtrail);
    assertThat(toStringList(result), contains("IAMUser", "EX_PRINCIPAL_ID", "arn:aws:iam::123456789012:user/Alice", "EXAMPLE_KEY_ID_ALICE"));
  }

  @Test
  public void negativeStartSlice() {
    JsonNode result = evaluate("Records[0].userIdentity.* | [-3:4]", cloudtrail);
    assertThat(toStringList(result), contains("EXAMPLE_KEY_ID_ALICE"));
  }

  @Test
  public void negativeStepSliceReversesOrder() {
    JsonNode result = evaluate("Records[0].userIdentity.* | [::-2]", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "EXAMPLE_KEY_ID_ALICE", "EX_PRINCIPAL_ID"));
  }

  @Test
  public void currentNodeReturnsInput() {
    JsonNode result = evaluate("@", cloudtrail);
    assertThat(result.get("Records").size(), is(3));
  }

  @Test
  public void currentNodeAsNoOp() {
    JsonNode result = evaluate("@ | Records[0].userIdentity | @ | userName | @ | @", cloudtrail);
    assertThat(result.textValue(), is("Alice"));
  }

  @Test
  public void andReturnsSecondOperandWhenFirstIsTruthy() {
    JsonNode result = evaluate("Records[0].userIdentity.userName && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.textValue(), is("Bob"));
  }

  @Test
  public void andReturnsFirstOperandWhenItIsFalsy() {
    JsonNode result = evaluate("'' && Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.textValue(), is(""));
  }

  @Test
  public void aLongChainOfAnds() {
    JsonNode result = evaluate("@ && Records[2] && Records[2].responseElements && Records[2].responseElements.keyName", cloudtrail);
    assertThat(result.textValue(), is("mykeypair"));
  }

  @Test
  public void orReturnsFirstOperandWhenItIsTruthy() {
    JsonNode result = evaluate("Records[0].userIdentity.userName || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.textValue(), is("Alice"));
  }

  @Test
  public void orReturnsSecondOperandWhenFirstIsFalsy() {
    JsonNode result = evaluate("'' || Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.textValue(), is("Bob"));
  }

  @Test
  public void aLongChainOfOrs() {
    JsonNode result = evaluate("'' || Records[3] || Records[2].foobar || Records[2].responseElements.keyName", cloudtrail);
    assertThat(result.textValue(), is("mykeypair"));
  }

  @Test
  public void selectionWithTrueTest() {
    JsonNode result = evaluate("Records[?@]", cloudtrail);
    assertThat(result.isArray(), is(true));
    assertThat(result.size(), is(3));
  }

  @Test
  public void selectionWithBooleanProperty() {
    JsonNode result = evaluate("Records[*] | [?userIdentity.sessionContext.attributes.mfaAuthenticated].eventTime", cloudtrail);
    assertThat(toStringList(result), contains("2014-03-06T17:10:34Z"));
  }

  @Test
  public void selectionWithFalseTest() {
    JsonNode result = evaluate("Records[?'']", cloudtrail);
    assertThat(result.isArray(), is(true));
    assertThat(result.size(), is(0));
  }

  @Test
  public void selectionStartsProjection() {
    JsonNode result = evaluate("Records[?@].userIdentity.userName", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void selectionTestReferencingProperty() {
    JsonNode result = evaluate("Records[*].responseElements | [?keyFingerprint]", cloudtrail);
    assertThat(result.isArray(), is(true));
    assertThat(result.size(), is(1));
    assertThat(result.get(0).get("keyName").textValue(), is("mykeypair"));
  }

  @Test
  public void selectionDoesNotSelectProjectionPutEachProjectedElement() {
    JsonNode result = evaluate("Records[*].responseElements.keyName[?@]", cloudtrail);
    assertThat(result.isArray(), is(true));
    assertThat(result.size(), is(0));
  }

  @Test
  public void selectionOnNonArrayProducesNull() {
    JsonNode result = evaluate("Records[0].userIdentity[?@]", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void selectionWithComplexTest() {
    JsonNode result = evaluate("Records[*] | [?userIdentity.userName == 'Bob' || responseElements.instancesSet.items[0].instanceId == 'i-ebeaf9e2'].userIdentity.userName", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob"));
  }

  @Test
  public void compareEqualityWhenEqualProducesTrue() {
    JsonNode result = evaluate("Records[0].userIdentity.userName == Records[2].userIdentity.userName", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareEqualityWhenNotEqualProducesFalse() {
    JsonNode result = evaluate("Records[0].userIdentity.userName == Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNonEqualityWhenEqualProducesFalse() {
    JsonNode result = evaluate("Records[0].userIdentity.userName != Records[2].userIdentity.userName", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNonEqualityWhenNotEqualProducesTrue() {
    JsonNode result = evaluate("Records[0].userIdentity.userName != Records[1].userIdentity.userName", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersEqWhenEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code == currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersEqWhenNotEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code == previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersNotEqWhenEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code != currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersNotEqWhenNotEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code != previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersGtWhenGt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code > previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersGtWhenLt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code > currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersGteWhenGt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code >= previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersGteWhenEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code >= currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersGteWhenLt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code >= currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersLtWhenGt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code < previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersLtWhenLt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code < currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersLteWhenGt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code <= previousState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void compareNumbersLteWhenEq() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | currentState.code <= currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareNumbersLteWhenLt() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState.code <= currentState.code", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void compareGtWithNonNumberProducesNull() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState > currentState", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void compareGteWithNonNumberProducesNull() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState >= currentState", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void compareLtWithNonNumberProducesNull() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState < currentState", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void compareLteWithNonNumberProducesNull() {
    JsonNode result = evaluate("Records[1].responseElements.instancesSet.items[0] | previousState <= currentState", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void negateSomethingTruthyProducesFalse() {
    JsonNode result = evaluate("!'hello'", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(false));
  }

  @Test
  public void negateNullProducesTrue() {
    JsonNode result = evaluate("!Records[3]", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void negateEmptyStringProducesTrue() {
    JsonNode result = evaluate("!''", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void negateEmptyArrayProducesTrue() {
    JsonNode result = evaluate("Records[?''] | !@", cloudtrail);
    assertThat(result.isBoolean(), is(true));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void createObject() {
    JsonNode result = evaluate("{userNames: Records[*].userIdentity.userName, keyName: Records[2].responseElements.keyName}", cloudtrail);
    assertThat(toStringList(result.get("userNames")), contains("Alice", "Bob", "Alice"));
    assertThat(result.get("keyName").textValue(), is("mykeypair"));
  }

  @Test
  public void createObjectInPipe() {
    JsonNode result = evaluate("Records[*].userIdentity | {userNames: [*].userName, anyUsedMfa: ([?sessionContext.attributes.mfaAuthenticated] | !!@)}", cloudtrail);
    assertThat(toStringList(result.get("userNames")), contains("Alice", "Bob", "Alice"));
    assertThat(result.get("anyUsedMfa").booleanValue(), is(true));
  }

  @Test
  public void createObjectInProjection() {
    JsonNode result = evaluate("Records[*].userIdentity.{userName: userName, usedMfa: sessionContext.attributes.mfaAuthenticated}", cloudtrail);
    assertThat(result.get(0).get("usedMfa").isNull(), is(true));
    assertThat(result.get(1).get("usedMfa").isNull(), is(true));
    assertThat(result.get(2).get("usedMfa").booleanValue(), is(true));
  }

  @Test
  public void nestedCreateObject() {
    JsonNode result = evaluate("Records[*].userIdentity | {users: {names: [*].userName}}", cloudtrail);
    assertThat(toStringList(result.get("users").get("names")), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void createObjectOnNullProducesNull() {
    JsonNode result = evaluate("bork.{foo: bar}", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void createArray() {
    JsonNode result = evaluate("[Records[*].userIdentity.userName, Records[2].responseElements.keyName]", cloudtrail);
    assertThat(toStringList(result.get(0)), contains("Alice", "Bob", "Alice"));
    assertThat(result.get(1).textValue(), is("mykeypair"));
  }

  @Test
  public void createArrayInPipe() {
    JsonNode result = evaluate("Records[*].userIdentity | [[*].userName, ([?sessionContext.attributes.mfaAuthenticated] | !!@)]", cloudtrail);
    assertThat(toStringList(result.get(0)), contains("Alice", "Bob", "Alice"));
    assertThat(result.get(1).booleanValue(), is(true));
  }

  @Test
  public void createArrayInProjection() {
    JsonNode result = evaluate("Records[*].userIdentity.[userName, sessionContext.attributes.mfaAuthenticated]", cloudtrail);
    assertThat(result.get(0).get(1).isNull(), is(true));
    assertThat(result.get(1).get(1).isNull(), is(true));
    assertThat(result.get(2).get(1).booleanValue(), is(true));
  }

  @Test
  public void nestedCreateArray() {
    JsonNode result = evaluate("Records[*].userIdentity | [[*].type, [[*].userName]]", cloudtrail);
    assertThat(toStringList(result.get(0)), contains("IAMUser", "IAMUser", "IAMUser"));
    assertThat(toStringList(result.get(1).get(0)), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void createArrayOnNullProducesNull() {
    JsonNode result = evaluate("bork.[snork]", cloudtrail);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void jsonLiteralNumber() {
    JsonNode result = evaluate("`42`", parseString("{}"));
    assertThat(result.intValue(), is(42));
  }

  @Test
  public void jsonLiteralString() {
    JsonNode result = evaluate("`\"foo\"`", parseString("{}"));
    assertThat(result.textValue(), is("foo"));
  }

  @Test
  public void jsonLiteralBoolean() {
    JsonNode result = evaluate("`true`", parseString("{}"));
    assertThat(result.booleanValue(), is(true));
  }

  @Test
  public void jsonLiteralArray() {
    JsonNode result = evaluate("`[42, \"foo\", true]`", parseString("{}"));
    assertThat(result, is(parseString("[42, \"foo\", true]")));
  }

  @Test
  public void jsonLiteralObject() {
    JsonNode result = evaluate("`{\"n\": 42, \"s\": \"foo\", \"b\": true}`", parseString("{}"));
    assertThat(result, is(parseString("{\"n\": 42, \"s\": \"foo\", \"b\": true}")));
  }

  @Test
  public void jsonLiteralInComparison() {
    JsonNode result = evaluate("Records[?requestParameters == `{\"keyName\":\"mykeypair\"}`].sourceIPAddress", cloudtrail);
    assertThat(toStringList(result), contains("72.21.198.64"));
  }

  @Test
  public void callFunction() {
    JsonNode result = evaluate("type(@)", parseString("{}"));
    assertThat(result.textValue(), is("object"));
  }

  @Test(expected = FunctionCallException.class)
  public void callNonExistentFunctionThrowsFunctionCallException() {
    evaluate("bork()", parseString("{}"));
  }

  @Test
  public void callFunctionWithExpressionReference() {
    JsonNode result = evaluate("map(&userIdentity.userName, Records)", cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob", "Alice"));
  }
}
