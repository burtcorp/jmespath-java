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

import io.burt.jmespath.AstGenerator;
import io.burt.jmespath.Query;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.contains;

public class JacksonAdapterTest {
  private JsonNode contact;
  private JsonNode cloudtrail;
  private JacksonAdapter adapter;

  private JsonNode loadExample(String path) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      return objectMapper.readTree(getClass().getResource(path));
    } catch (IOException ioe) {
      fail(String.format("Failed parsing %s: \"%s\"", path, ioe.getMessage()));
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

  @Before
  public void beforeEach() {
    contact = loadExample("/contact.json");
    cloudtrail = loadExample("/cloudtrail.json");
    adapter = new JacksonAdapter();
  }

  @Test
  public void topLevelProperty() {
    Query query = AstGenerator.fromString("lastName");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.asText(), is("Smith"));
  }

  @Test
  public void chainProperty() {
    Query query = AstGenerator.fromString("address.state");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.asText(), is("NY"));
  }

  @Test
  public void propertyNotFound() {
    Query query = AstGenerator.fromString("address.country");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void nullValue() {
    Query query = AstGenerator.fromString("spouse");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void index() {
    Query query = AstGenerator.fromString("phoneNumbers[1].type");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.asText(), is("office"));
  }

  @Test
  public void indexNotFound() {
    Query query = AstGenerator.fromString("phoneNumbers[3].type");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(result.isNull(), is(true));
  }

  @Test
  public void projection() {
    Query query = AstGenerator.fromString("phoneNumbers[*].type");
    JsonNode result = query.evaluate(adapter, contact);
    assertThat(toStringList(result), contains("home", "office", "mobile"));
  }

  @Test
  public void multiStepProjection() {
    Query query = AstGenerator.fromString("Records[*].userIdentity.userName");
    JsonNode result = query.evaluate(adapter, cloudtrail);
    assertThat(toStringList(result), contains("Alice", "Bob", "Alice"));
  }

  @Test
  public void projectionFiltersNull() {
    Query query = AstGenerator.fromString("Records[*].requestParameters.keyName");
    JsonNode result = query.evaluate(adapter, cloudtrail);
    assertThat(toStringList(result), contains("mykeypair"));
  }

  @Test
  public void pipeStopsProjections() {
    Query query = AstGenerator.fromString("Records[*].userIdentity | [1].userName");
    JsonNode result = query.evaluate(adapter, cloudtrail);
    assertThat(result.asText(), is("Bob"));
  }

  @Test
  public void literalString() {
    Query query = AstGenerator.fromString("'hello world'");
    JsonNode result = query.evaluate(adapter, cloudtrail);
    assertThat(result.asText(), is("hello world"));
  }

  @Test
  public void literalStringIgnoresSource() {
    Query query = AstGenerator.fromString("Records[*] | 'hello world'");
    JsonNode result = query.evaluate(adapter, cloudtrail);
    assertThat(result.asText(), is("hello world"));
  }
}
