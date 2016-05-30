package io.burt.jmespath.jp;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonParseException;

import io.burt.jmespath.Query;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathException;
import io.burt.jmespath.jackson.JacksonAdapter;

public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length == 0 || args[0].length() == 0) {
      System.exit(1);
    }
    try {
      JsonNode input = new ObjectMapper().readTree(System.in);
      Adapter<JsonNode> adapter = new JacksonAdapter();
      Query query = Query.fromString(args[0]);
      JsonNode result = query.evaluate(adapter, input);
      System.out.println(result.toString());
    } catch (JsonParseException jpe) {
      System.err.println("Bad input: " + jpe.getMessage());
      System.exit(1);
    } catch (JmesPathException jpe) {
      System.err.println("Bad query: " + jpe.getMessage());
      System.exit(1);
    }
  }
}
