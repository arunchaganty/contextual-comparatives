# This script reproduces the results presented in Table 5(b) of the
# paper, which evaluates the perspective generation component.

source util.lib

TRAIN_DATA=data/generation_pairs_train.tsv
TEST_DATA=data/generation_pairs_test.tsv
TRAIN_PARAMS=data/rnn.params
WORK_DIR=work_dir

assert_exists $TRAIN_DATA
assert_exists $TEST_DATA
ensure_dir $WORK_DIR

# Preproccess the training data
python bin/preprocess_generation_training_input.py --input $TRAIN_DATA --output $WORK_DIR/rnn_train.input
python bin/preprocess_generation_input.py --input $TRAIN_DATA --output $WORK_DIR/generation_train.input
python bin/preprocess_generation_input.py --input $TEST_DATA --output $WORK_DIR/generation_test.input

# First run the Baseline system.
python bin/generation_baseline.py --input $WORK_DIR/generation_train.input --output $WORK_DIR/baseline_train.output
python bin/generation_baseline.py --input $WORK_DIR/generation_test.input --output $WORK_DIR/baseline_test.output

# Next run the RNN system.
bash bin/run_generation.sh $WORK_DIR/rnn_train.input $TRAIN_PARAMS $WORK_DIR/generation_train.input $WORK_DIR/rnn_train.output
bash bin/run_generation.sh $WORK_DIR/rnn_train.input $TRAIN_PARAMS $WORK_DIR/generation_test.input $WORK_DIR/rnn_test.output

# Run evaluations
bash bin/evaluate_bleu.sh $WORK_DIR/baseline_train.output $TRAIN_DATA $WORK_DIR/baseline_train.bleu
bash bin/evaluate_bleu.sh $WORK_DIR/baseline_test.output  $TEST_DATA $WORK_DIR/baseline_test.bleu
bash bin/evaluate_bleu.sh $WORK_DIR/rnn_train.output $TRAIN_DATA $WORK_DIR/rnn_train.bleu
bash bin/evaluate_bleu.sh $WORK_DIR/rnn_test.output  $TEST_DATA $WORK_DIR/rnn_test.bleu

# Finally, build table.
echo "system        train-bleu       test-bleu" > table5b.tsv
echo "Baseline	$(cut -f2 $WORK_DIR/baseline_train.bleu)	$(cut -f2 $WORK_DIR/baseline_test.bleu)	" >> table5b.tsv
echo "RNN	$(cut -f2 $WORK_DIR/rnn_train.bleu)	$(cut -f2 $WORK_DIR/rnn_test.bleu)	" >> table5b.tsv

info "Table 5b ready!"
cat table5b.tsv
