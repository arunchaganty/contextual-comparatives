for features in length-bias,fact-id  length-bias,fact-cross length-bias,numeric-proximity length-bias,arg-ssimilarity length-bias,fact-id,fact-cross length-bias,fact-id,fact-cross,numeric-proximity length-bias,fact-id,fact-cross,numeric-proximity,arg-ssimilarity; do
  bin/run.sh edu.stanford.nlp.perspectives.LearnRanking -input data/rank_majority.tsv -embeddings data/word_embeddings.tsv -features $features -outputPredictions output-$features.tsv | head -n 2
done | tee conf-output.txt
#for features in length-bias,fact-id  length-bias,fact-cross length-bias,numeric-proximity length-bias,arg-ssimilarity length-bias,fact-id,fact-cross length-bias,fact-id,fact-cross,numeric-proximity length-bias,fact-id,fact-cross,numeric-proximity,arg-ssimilarity; do
#  bin/javanlp.sh edu.stanford.nlp.arguments.comparatives.LearnRankingConfidence -input data/rank_confidence.tsv -embeddings data/word_embeddings.tsv -features $features | head -n 2
#done | tee mse-output.txt
