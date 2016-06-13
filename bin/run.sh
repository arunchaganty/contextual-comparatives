#!/bin/bash
JAVA_PATH=$JAVA_HOME/bin

deps=
for f in lib/*.jar; do
  deps=$f:$deps;
done;
for f in lib/CoreNLP/*.jar; do
  deps=$f:$deps;
done;
for f in lib/CoreNLP/lib/*.jar; do
  deps=$f:$deps;
done;
for f in lib/CoreNLP/liblocal/*.jar; do
  deps=$f:$deps;
done;

$JAVA_PATH/java -ea -Xms6g -Xmx6g -cp out/production/contextual-comparatives:$deps $@

