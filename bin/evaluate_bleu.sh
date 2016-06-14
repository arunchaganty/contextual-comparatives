#!/bin/bash
# Evaluates generation on BLEU score.
generated_output=$1
reference_output=$2
output=$3

# Make temporary BLEU scoring output
python bin/make_bleu_data.py --input $generated_output --references $reference_output --output .bleu.input
bash bin/run.sh edu.stanford.nlp.util.JBLEU -input .bleu.input > $output
