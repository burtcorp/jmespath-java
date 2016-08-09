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

### Adding custom functions

In addition to the built in functions like `sort`, `to_string`, and `sum` you can add your own. All you need to do is to create a class that extends `io.burt.jmespath.function.JmesPathFunction` and then register it with your adapter.

Here's how you add a `sin` function:

```java
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.ArgumentConstraints;
import io.burt.jmespath.function.ExpressionOrValue;
import io.burt.jmespath.function.JmesPathFunction;

// Functions must extend JmesPathFunction
public class SinFunction extends JmesPathFunction {
  public SinFunction() {
    // This is how you tell the type checker what arguments your function accepts
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER);
  }

  @Override
  protected <T> T callFunction(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) {
    // Arguments can be either values or expressions, but most functions only
    // accept expressions. You don't need to do any type checking here, the
    // the runtime has made sure that if this code runs the types are correct.
    T value = arguments.get(0).value();
    // Since we want to be able to use this function with all types of inputs
    // it needs to use the adapter to convert data types.
    double n = adapter.toNumber(value).doubleValue();
    // This is the actual function, the rest is wrapping, that's the price of
    // being generic and supporting multiple implementations.
    // There are abstract classes in the io.burt.jmespath.function
    // package that can be used to avoid having to write all of the wrapping
    // for some types of functions. This function could extend MathFunction,
    // for example.
    double s = Math.sin(n);
    // We must not forget to wrap the result using the adapter.
    return adapter.createNumber(s);
  }
}

// …

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.Query;
import io.burt.jmespath.Adapter;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.jackson.JacksonAdapter;

// There's a default registry that contains the built in JMESPath functions
FunctionRegistry defaultFunctions = FunctionRegistry.defaultRegistry();
// And we can create a new registry with additional functions by extending it
FunctionRegistry customFunctions = defaultFunctions.extend(new SinFunction());
// We need to tell the adapter to use our custom registry
Adapter<JsonNode> adapter = new JacksonAdapter(functionRegistry);
// Now the function is available in expressions
JsonNode result = Query.fromString(adapter, "sin(measurements.angle)").evaluate(adapter, input);
```

You can provide a name for your function, but the default is that the name will be the snake cased version of the class name, minus the "Function" suffix. `SinFunction` becomes `sin`, `MyAwesomeFunction` becomes `my_awesome`, etc.

Your function class needs to tell the runtime about what arguments it accepts. The function in the example above specifies that it accepts a single number as argument. Have a look at the existing functions and the documentation for the `ArgumentConstraints` DSL to see what is possible.

## How to build and run the tests

The best place to see how to build and run the tests is to look at the `.travis.yml` file, but if you just want to get going run:

```
$ mvn test
```

And all dependencies should be installed, the code compiled and the tests run.

# Copyright

© 2016 Burt AB, see LICENSE.txt (BSD 3-Clause).