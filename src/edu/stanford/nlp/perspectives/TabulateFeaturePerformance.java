package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util.WordEmbedding;
import edu.stanford.nlp.classify.*;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.ConfusionMatrix;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Ranking function for numeric mention expressions
 */
public class TabulateFeaturePerformance implements Runnable {
  @ArgumentParser.Option(name="output")
  public OutputStreamWriter output = new OutputStreamWriter(System.out);

  public void printOutput(LinearClassifier<Boolean, String> classifier, Featurizer f, List<NumericMentionExpression> rawDataset) throws IOException {
    final WeightedRVFDataset<Boolean, String> dataset = new WeightedRVFDataset<>();

    // Featurize dataset
    rawDataset.stream().forEach(e -> {
      dataset.add(f.featurize(e));
    });
    // Classify output.
    for(int i = 0; i < rawDataset.size(); i++) {
      NumericMentionExpression expression = rawDataset.get(i);
      Datum<Boolean, String> datum = dataset.getDatum(i);
      boolean useful = classifier.classOf(datum);
      output.write(String.format("%d\t%d\t%d\n", expression.id, (expression.label.get() > 0.5) ? 1 : 0, useful ? 1 : 0));
    }
  }
  @Override
  public void run() {
    LearnRanking learner = new LearnRanking();
//    LearnRankingConfidence learnerC = new LearnRankingConfidence();

    List<NumericMentionExpression> rawDataset;
    List<NumericMentionExpression> rawDataset1;
    List<NumericMentionExpression> rawDataset2;
    List<NumericMentionExpression> rawDataset3;
    try {
      learner.embeddings = (LearnRanking.embFile.length() > 0) ? WordEmbedding.loadFromFile(LearnRanking.embFile) : WordEmbedding.empty();
//      learnerC.embeddings = learner.embeddings;
      LearnRankingConfidence.balanceDataset = LearnRanking.balanceDataset;

      rawDataset = NumericMentionExpression.readFromTSV(LearnRanking.input);
      rawDataset1 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 1).collect(Collectors.toList());
      rawDataset2 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 2).collect(Collectors.toList());
      rawDataset3 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 3).collect(Collectors.toList());
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }

    LinearClassifierFactory<Boolean, String> factory = new LinearClassifierFactory<>();
//    LinearRegressionFactory<String> factoryR = new LinearRegressionFactory<>();

    // Bias + features
    //for(String templates : Featurizer.templateOptions) {
    //  Featurizer f = new Featurizer(learner.embeddings, "length-bias, " + templates);
    //  LearnRanking.featureTemplates = "length-bias, " + templates;
    //  LearnRankingConfidence.featureTemplates = "length-bias, " + templates;
    //  learner.run(factory, f, rawDataset);
    //  learnerC.run(factoryR, f, rawDataset);
    //  learner.run(factory, f, rawDataset1);
    //  learnerC.run(factoryR, f, rawDataset1);
    //  learner.run(factory, f, rawDataset2);
    //  learnerC.run(factoryR, f, rawDataset2);
    //  learner.run(factory, f, rawDataset3);
    //  learnerC.run(factoryR, f, rawDataset3);
    //}

    // Special sets of features
    List<String> featureSet = Arrays.asList(
        //"length-bias, fact-id, fact-cross",
        "length-bias, fact-mention-wv",
        "length-bias, fact-id, fact-cross, fact-mention-wv",
        "length-bias, fact-id, fact-mention-wv"
//        "length-bias, fact-id, fact-cross,arg-wv",
//        "length-bias, fact-id, fact-cross,response-wv",
//        "length-bias, fact-id, fact-cross,arg-cross-wv"
//        "length-bias, arg-ssimilarity, arg-cross-ssimilarity",
//        "length-bias, fact-id, fact-cross,arg-ssimilarity, arg-cross-ssimilarity"
//        "length-bias, fact-id, fact-cross,arg-ssimilarity, arg-cross-ssimilarity"
//        "length-bias, arg-wv, arg-cross-wv",
//        "length-bias, fact-id, fact-cross, arg-wv, arg-cross-wv"
//        String.join(",", templateOptions) // Everything and the kitchen sink
    );

    for(String templates : featureSet) {
      Featurizer f = new Featurizer(learner.embeddings, templates);
      LearnRanking.featureTemplates = templates;
//      LearnRankingConfidence.featureTemplates = templates;
      LinearClassifier<Boolean, String> classifier = learner.run(factory, f, rawDataset);
      // Run classifier on all the input data, and output results.
      try {
        output.write(templates + "\n");
        printOutput(classifier, f, rawDataset);
        output.write("\n");
      } catch (IOException e) {
        e.printStackTrace();
      }

//      learnerC.run(factoryR, f, rawDataset);
//      learner.run(factory, f, rawDataset1);
//      learnerC.run(factoryR, f, rawDataset1);
//      learner.run(factory, f, rawDataset2);
//      learnerC.run(factoryR, f, rawDataset2);
//      learner.run(factory, f, rawDataset3);
//      learnerC.run(factoryR, f, rawDataset3);
    }
  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(LearnRanking.class, args);
    new TabulateFeaturePerformance().run();
  }
}
