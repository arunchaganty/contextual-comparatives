# Takes a list of features as input and post-processes output to
# display for CodaLab.

TRAIN_DATA=rank_majority.tsv
EMB=word_embeddings.tsv

features=$1;    # Full feature specification.
fname=$2;       # Short, human readable description.

# Run the learning procedure.
bash run.sh edu.stanford.nlp.perspectives.LearnRanking \
  -input $TRAIN_DATA \
  -embeddings $EMB \
  -outputPredictions /dev/null\
  -output model.params\
  -features $features >\
  output.log

# Process the output to fit into the tsv format.
echo "name	$fname" > output.tab
head -n 2 output.log | python transpose.py >> output.tab

