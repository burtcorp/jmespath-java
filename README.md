# jmespath-java

[![Build Status](https://travis-ci.org/burtcorp/jmespath-java.png?branch=master)](https://travis-ci.org/burtcorp/jmespath-java)

_If you're reading this on GitHub, please note that this is the readme for the development version and that some features described here might not yet have been released. You can find the readme for a specific version via the release tags ([here is an example](https://github.com/burtcorp/jmespath-java/releases/tag/jmespath-0.1.0))._

This is an implementation of [JMESPath](http://jmespath.org/) for Java and it supports searching JSON documents (via [Jackson](https://github.com/FasterXML/jackson) or [Gson](https://github.com/google/gson)) and structures containing basic Java objects (`Map`, `List`, `String`, etc.) – but can also be extended to work with any JSON-like structure.

## Installation

Using Maven you can add this to your dependencies:

```xml
<dependency>
  <groupId>io.burt</groupId>
  <artifactId>jmespath</artifactId>
  <version>${jmespath.version}</version>
</dependency>
```

Check the [releases page](https://github.com/burtcorp/jmespath-java/releases) for the value of `${jmespath.version}`.

If you don't want both the Jackson and Gson implementations you can change it to `jmespath-jackson` or `jmespath-gson`.

### Dependencies

`jmespath-core` has an ANTLR based parser, but the ANTLR runtime artifact has been shaded into the `io.burt.jmespath` package to avoid conflicts with other artifacts that may depend on ANTLR. This means that `jmespath-core` has no external dependencies.

`jmespath-jackson` obviously depends on Jackson, specifically Jackson DataBind (`com.fasterxml.jackson.core:jackson-databind`), but other than that it only depends on `jmespath-core`.

`jmespath-gson` depends on GSON, specifically `com.google.code.gson:gson`, but other than that only `jmespath-core`.

## Basic usage

```java
import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.Expression;
import io.burt.jmespath.jackson.JacksonRuntime;

// …

// The first thing you need is a runtime. These objects can compile expressions
// and they are specific to the kind of structure you want to search in.
// For most purposes you want the Jackson runtime, it can search in JsonNode
// structures created by Jackson.
JmesPath<JsonNode> jmespath = new JacksonRuntime();
// Expressions need to be compiled before you can search. Compiled expressions
// are reusable and thread safe. Compile your expressions once, just like database
// prepared statements.
Expression<JsonNode> expression = jmespath.compile("locations[?state == 'WA'].name | sort(@) | {WashingtonCities: join(', ', @)}");
// This you have to fill in yourself, you're probably using Jackson's ObjectMapper
// to load JSON data, and that should fit right in here.
JsonNode input = …;
// Finally this is how you search a structure. There's really not much more to it.
JsonNode result = expression.search(input);
```

## Description

`jmespath-java` comes in three parts: `jmespath-core`, `jmespath-jackson`, and `jmespath-gson`. The former contains the expression parser, core runtime, default functions and a simple runtime adapter that can search structures made up from numbers, strings, booleans, `List` and `Map` available as `io.burt.jmespath.jcf.JcfRuntime` (for "Java Collections Framework"). The latter contains the Jackson and GSON runtime adapters, respectively, and is what you should be using most of the time. The JCF runtime is just for internal development and testing. It primarily exists to test that there's nothing runtime-specific in the implementation.

## Extensions

`jmespath-java` is designed to be extensible. You can extend it in two ways: by adding new functions, and by creating different runtime adapters. These are not mutually exclusive, if you write your custom functions the right way you can use them with any runtime, and vice-versa.

### Adding custom functions

In addition to the built in functions like `sort`, `to_string`, and `sum` you can add your own. All you need to do is to create a class that extends `io.burt.jmespath.function.BaseFunction` (actually implement `Function` from the same package, but then you'd need to do much more work yourself) and then register it with your runtime.

Here's how you add a `sin` function:

```java
import java.util.List;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.JmesPathType;
import io.burt.jmespath.function.BaseFunction;
import io.burt.jmespath.function.FunctionArgument;
import io.burt.jmespath.function.ArgumentConstraints;

// Functions must implement Function, for example by extending BaseFunction
public class SinFunction extends BaseFunction {
  public SinFunction() {
    // This is how you tell the type checker what arguments your function accepts
    super(ArgumentConstraints.typeOf(JmesPathType.NUMBER);
  }

  @Override
  protected <T> T callFunction(Adapter<T> runtime, List<FunctionArgument<T>> arguments) {
    // Arguments can be either values or expressions, but most functions only
    // accept expressions. You don't need to do any type checking here, the
    // the runtime has made sure that if this code runs the types are correct.
    T value = arguments.get(0).value();
    // Since we want to be able to use this function with all types of inputs
    // it needs to use the runtime to convert data types.
    double n = runtime.toNumber(value).doubleValue();
    // This is the actual function, the rest is wrapping, that's the price of
    // being generic and supporting multiple implementations.
    // There are abstract classes in the io.burt.jmespath.function
    // package that can be used to avoid having to write all of the wrapping
    // for some types of functions. This function could extend MathFunction,
    // for example.
    double s = Math.sin(n);
    // We must not forget to wrap the result using the runtime.
    return runtime.createNumber(s);
  }
}

// …

import com.fasterxml.jackson.databind.JsonNode;

import io.burt.jmespath.JmesPath;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.jackson.JacksonRuntime;

// There's a default registry that contains the built in JMESPath functions
FunctionRegistry defaultFunctions = FunctionRegistry.defaultRegistry();
// And we can create a new registry with additional functions by extending it
FunctionRegistry customFunctions = defaultFunctions.extend(new SinFunction());
// We need to create a runtime that uses our custom registry
JmesPath<JsonNode> runtime = new JacksonRuntime(functionRegistry);
// Now the function is available in expressions
JsonNode result = runtime.compile("sin(measurements.angle)").search(input);
```

You can provide a name for your function, but the default is that the name will be the snake cased version of the class name, minus the "Function" suffix. `SinFunction` becomes `sin`, `MyAwesomeFunction` becomes `my_awesome`, etc.

Your function class needs to tell the runtime about what arguments it accepts. The function in the example above specifies that it accepts a single number as argument. Have a look at the existing functions and the documentation for the `ArgumentConstraints` DSL to see what is possible.

### Creating a runtime adapter

Creating a runtime adapter is a bit more work than adding a function, but not extremely so. What you need to do is to implement the `io.burt.jmespath.Adapter` interface. The easiest is to start by extending `io.burt.jmespath.BaseRuntime`, that way you don't need to implement some of the things that are common to most runtimes, like comparing values.

Most of the work a runtime does is converting back and forth between Java types. The core runtime can't know about all of the types of structures you want to search, but it knows that they will be JSON-like, so it uses the runtime adapters to help translate. The Jackson runtime adapter, for example, translates the world of `JsonNode`s into Java types like `List` and `String` when asked by the core runtime.

Runtime adapters can wrap libraries like [Jackson](https://github.com/FasterXML/jackson) and [Gson](https://github.com/google/gson), but can also make it possible, for example, to search Java beans with JMESPath by translating JMESPath operations to reflection calls. The structure to search doesn't have to be JSON, it just has to be JSON-_like_.

A good starting point for writing a new runtime adapter is reading the code of the existing adapters and the docs for `Adapter` and `BaseAdapter`. There are also JUnit tests in `JmesPathRuntimeTest` and `JmesPathComplianceTest` that can be subclassed and run against any runtime.

## How to build and run the tests

The best place to see how to build and run the tests is to look at the `.travis.yml` file, but if you just want to get going run:

```
$ git submodule update --init --recursive
$ mvn test
```

And all dependencies should be installed, the code compiled and the tests run.

# Copyright

© 2016-2018 Burt AB, see LICENSE.txt (BSD 3-Clause).
