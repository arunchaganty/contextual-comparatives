#!/bin/bash
# Runs the RNN generation system.
training_data=$1
training_params=$2
input=$3
output=$4

python nn/src/py/main.py -d 100 -i 50 -o 50 -t 20 -p attention -c lstm -m attention -u 1 \
--no-train \
--train-data $training_data --dev-frac 0.2 \
--load-file $training_params \
--input $input \
--output $output
