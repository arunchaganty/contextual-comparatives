# This script reproduces the results presented in Table 7 of the
# paper, which evaluates the system end to end.

source util.lib

WORK_DIR=work_dir
GEN_PARAMS=data/rnn.params
GEN_TRAIN=$WORK_DIR/rnn_train.input

SEL_PARAMS=data/model.params
TEST_DATA=data/mentions_eval.tsv

assert_exists $GEN_TRAIN
assert_exists $GEN_PARAMS
assert_exists $SEL_PARAMS
assert_exists $TEST_DATA

# First get the formula selections
bash bin/run_formula_selection.sh $SEL_PARAMS $TEST_DATA $WORK_DIR/end2end_formulas.tsv

# Rename the mention_id field to id
cut -f 1,2 $WORK_DIR/end2end_formulas.tsv | sed -e "s/mention_id/id/" > $WORK_DIR/end2end_generation.input

# Then run the generation system to get the perspectives
python bin/generation_baseline.py --input $WORK_DIR/end2end_generation.input --output $WORK_DIR/end2end_baseline.output
bash bin/run_generation.sh $GEN_TRAIN $GEN_PARAMS $WORK_DIR/end2end_generation.input  $WORK_DIR/end2end_rnn.output

# Now use the turked data to evaluate these (of course this doesn't
# actually require the output obtained above). 
python bin/summarize_evaluation.py --input data/end2end_evaluation.tsv --output table7a.tsv
python bin/summarize_error_analysis.py --input data/end2end_evaluation.tsv --output table7b.tsv

info "table 7a, 7b done!"
echo "== Table 7(a)" 
cat table7a.tsv

echo "== Table 7(b)" 
cat table7b.tsv
