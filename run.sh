#!/bin/bash
JAVA_PATH=$JAVA_HOME/bin

deps=
for f in lib/*.jar; do
  deps=$f:$deps;
done;
for f in lib/stanford-corenlp-full-2015-04-20/*.jar; do
  deps=$f:$deps;
done;
for f in CoreNLP/*.jar; do
  deps=$f:$deps;
done;

$JAVA_PATH/java -ea -Xms6g -Xmx6g -cp $deps:out/production/contextual-comparatives $@

