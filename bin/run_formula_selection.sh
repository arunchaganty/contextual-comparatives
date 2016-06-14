#!/bin/bash
# Runs the formula selection system
model=$1
input_mentions=$2
output=$3

./bin/run.sh edu.stanford.nlp.perspectives.RankComparisons \
     -tuples data/cc_numericdata_train.tsv \
     -embeddings data/word_embeddings.tsv \
     -units data/unit_conversions.tsv \
     -model $model \
     -features 'length-bias,fact-id,fact-cross,arg-ssimilarity,numeric-proximity' \
     -isClassifier true \
     -outputIds \
     -mode classifier \
     -input $input_mentions \
     -output $output

