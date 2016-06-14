# This script reproduces the results presented in Table 5(a) of the
# paper, which evaluates the formula selection component.

source util.lib

TRAIN_DATA=data/rank_majority.tsv
FEATURES=(
  length-bias,numeric-proximity
  length-bias,arg-ssimilarity
  length-bias,fact-id
  length-bias,fact-cross
  length-bias,fact-id,fact-cross
  length-bias,fact-id,fact-cross,numeric-proximity
  length-bias,fact-id,fact-cross,numeric-proximity,arg-ssimilarity
  );

assert_exists $TRAIN_DATA

# Create a header for the training results.
echo "features        train-acc       train-prec      train-rec       train-f1        test-acc       test-prec        test-rec        test-f1" > table5a.tsv

ensure_dir outputs
ensure_dir models
ensure_dir logs

# For each of the features.
for features in ${FEATURES[@]}; do
  assert "-n $features" "Invalid feature: $features";
  info "Learning model for $features";
  # Run the learning procedure.
  bin/run.sh edu.stanford.nlp.perspectives.LearnRanking -input data/rank_majority.tsv -embeddings data/word_embeddings.tsv -output models/$features.model -features $features -outputPredictions outputs/$features.tsv > logs/$features.log
  # Process the output to fit into the tsv format.
  tail -n 1 logs/$features.log >> table5a.tsv
done;

info "Table 5a ready!"
echo "=== Table 5(a)"
cat table5a.tsv
