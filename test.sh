#!/bin/bash

export CLASSPATH=/usr/local/opt/antlr/antlr-4.5.2-complete.jar:build

function parse() {
  (cd build && grun io.burt.jmespath.generated.JmesPath query -tree)
}

echo -n "hello.world" | parse
echo -n "locations[?state=='WA'].name|sort(@)|{WashingtonCities:join(', ',@)}" | parse
echo -n 'foo[?bar == `"baz"`]' | parse
