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

import io.burt.jmespath.AstGenerator;
import io.burt.jmespath.Query;

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
      strings.add(element.asText());
    }
    return strings;
  }

  private JsonNode evaluate(String query, JsonNode input) {
    return AstGenerator.fromString(query).evaluate(adapter, input);
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
    assertThat(result.asText(), is("Smith"));
  }

  @Test
  public void chainProperty() {
    JsonNode result = evaluate("address.state", contact);
    assertThat(result.asText(), is("NY"));
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
    assertThat(result.asText(), is("office"));
  }

  @Test
  public void negativeIndex() {
    JsonNode result = evaluate("phoneNumbers[-2].type", contact);
    assertThat(result.asText(), is("office"));
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
  public void pipeStopsProjections() {
    JsonNode result = evaluate("Records[*].userIdentity | [1].userName", cloudtrail);
    assertThat(result.asText(), is("Bob"));
  }

  @Test
  public void literalString() {
    JsonNode result = evaluate("'hello world'", cloudtrail);
    assertThat(result.asText(), is("hello world"));
  }

  @Test
  public void literalStringIgnoresSource() {
    JsonNode result = evaluate("Records[*] | 'hello world'", cloudtrail);
    assertThat(result.asText(), is("hello world"));
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
}
