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
Query query = Query.fromString(adapter, "locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
JsonNode result = query.evaluate(adapter, input);
```

## How to build and run the tests

The best place to see how to build and run the tests is to look at the `.travis.yml` file, but if you just want to get going run:

```
$ mvn test
```

And all dependencies should be installed, the code compiled and the tests run.

# Copyright

© 2016 Burt AB, see LICENSE.txt (BSD 3-Clause).