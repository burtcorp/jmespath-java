package io.burt.jmespath.ast;

import java.util.Map;
import java.util.LinkedHashMap;

public class MultiSelectHashNode extends JmesPathNode {
  private final Map<String, JmesPathNode> pairs;

  public static class KV extends JmesPathNode {
    private final String key;
    private final JmesPathNode value;

    public KV(String key, JmesPathNode value) {
      this.key = key;
      this.value = value;
    }

    public String key() { return key; }

    public JmesPathNode value() { return value; }
  }

  public MultiSelectHashNode(Map<String, JmesPathNode> pairs) {
    this.pairs = pairs;
  }

  public MultiSelectHashNode(KV... kvs) {
    this.pairs = new LinkedHashMap<>();
    for (KV kv : kvs) {
      this.pairs.put(kv.key(), kv.value());
    }
  }

  protected Map<String, JmesPathNode> pairs() {
    return pairs;
  }

  @Override
  public String toString() {
    StringBuilder pairsString = new StringBuilder();
    for (Map.Entry<String, JmesPathNode> entry : pairs.entrySet()) {
      pairsString.append(", ").append(entry.getKey()).append(" = ").append(entry.getValue());
    }
    pairsString.delete(0, 2);
    return String.format("MultiSelectHashNode(%s)", pairsString);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MultiSelectHashNode)) {
      return false;
    }
    MultiSelectHashNode other = (MultiSelectHashNode) o;
    return this.pairs().equals(other.pairs());
  }

  @Override
  public int hashCode() {
    int h = 1;
    h = h * 31 + pairs.hashCode();
    return h;
  }
}
