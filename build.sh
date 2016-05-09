#!/bin/bash

export CLASSPATH=/usr/local/opt/antlr/antlr-4.5.2-complete.jar

mkdir -p build

(cd src/main/antlr4 && antlr4 -o ../java/io/burt/jmespath/generated -package io.burt.jmespath.generated JmesPath.g4)
javac -d build -sourcepath src/main/java $(find src/main/java -name '*.java')
