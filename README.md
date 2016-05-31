# jmespath-java

An implementation of [JMESPath](http://jmespath.org/) for Java. It supports searching JSON documents (via Jackson) and structures containing basic Java objects (`Map`, `List`, `String`, etc.).

## Basic usage

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.Query;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.jackson.JacksonAdapter;

// …

JsonNode input = new ObjectMapper().readTree(System.in);
Adapter<JsonNode> adapter = new JacksonAdapter();
Query query = Query.fromString(args[0]);
JsonNode result = query.evaluate(adapter, input);
```

# Copyright

© 2016 Burt AB, see LICENSE.txt (BSD 3-Clause).