package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util.WordEmbedding;
import edu.stanford.nlp.classify.*;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Ranking function for numeric mention expressions
 */
public class LearnRankingConfidence implements Runnable {
  @ArgumentParser.Option(name="input", gloss="TSV file with input", required = true)
  public static String input;
  @ArgumentParser.Option(name="embeddings", gloss="TSV file with wv embeddings", required = true)
  public static String embFile;
  WordEmbedding embeddings;

  @ArgumentParser.Option(name="subset", gloss="choose a subset of the data")
  public static int subset = 0;

  @ArgumentParser.Option(name="features", gloss="comma separated list of features to use")
  public static String featureTemplates = "bias";

  @ArgumentParser.Option(name="outputFeatures", gloss="Output features")
  public static boolean outputFeatures = false;

  @ArgumentParser.Option(name="balance", gloss="Balance dataset")
  public static boolean balanceDataset = false;

  @ArgumentParser.Option(name="output", gloss="Save serialized model")
  public static String output = "";

  public double computeMSE(Regressor<String> regressor, GeneralDataset<Double, String> dataset) {
    double MSE = 0.;
    int count = 0;
    for(RVFDatum<Double, String> datum : dataset) {
      double err = datum.label() - regressor.valueOf(datum);
      MSE += (err*err - MSE)/(count + 1);
      count +=1;
    }
    return Math.sqrt(MSE);
  }

  public LinearRegressor<String> run(final LinearRegressionFactory<String> factory, final RVFDataset<Double, String> dataset) {
    CrossValidator<Double, String> cv = new CrossValidator<>(dataset, 10);
    cv.computeAverage(t -> {
      GeneralDataset<Double, String> train = t.first;
      GeneralDataset<Double, String> test = t.second;
      LinearRegressor<String> classifier = (LinearRegressor<String>) factory.train(train);

      double trainMSE = computeMSE(classifier, train);
      double testMSE = computeMSE(classifier, test);
      t.third.state = Triple.makeTriple(classifier, trainMSE, testMSE);
      return 0.;
    });

    Pair<Double, Double> pair = cv.getSavedStates().stream()
        .map(o_ -> {
          Triple<?, Double, Double> o = (Triple<?, Double, Double>) o_;
          return Pair.makePair(o.second, o.third);
        })
        .reduce(Pair.makePair(0., 0.),
            (acc, o) -> {
              acc.first += o.first / cv.getKFold();
              acc.second += o.second / cv.getKFold();
              return acc;});

    Counter<String> features = cv.getSavedStates().stream()
        .map(o_ -> ((Triple<LinearRegressor<String>, ?, ?>) o_).first)
        .map(c -> c.getFeatureWeights())
        .reduce(new ClassicCounter<>(), (acc, o) -> {
          acc.addAll(o);
          return acc;
        });
    features = Counters.scale(features, 1./cv.getKFold());

    // TODO(chaganty): compute P/R/F1
    log("Features: " + featureTemplates);
    log(String.format("Cross validated train MSE: %.3f",
        pair.first));
    log(String.format("Cross validated test MSE: %.3f",
        pair.second));
    System.out.println(String.join("\t", Arrays.asList(
        "features",
        "train-mse",
        "test-mse"
    )));
    System.out.println(String.join("\t", Arrays.asList(
        featureTemplates,
        String.format("%.3f", pair.first),
        String.format("%.3f", pair.second)
    )));

    return new LinearRegressor<>(features);
  }

  public LinearRegressor<String> run(final LinearRegressionFactory<String> factory, Featurizer f, List<NumericMentionExpression> rawDataset) {
    final WeightedRVFDataset<Double, String> dataset = new WeightedRVFDataset<>();

    float[] scale = {1, 1, 1};
    if(balanceDataset) {
      // Get counts for labels at each number of entries.
      float[] trueCnts = {0, 0, 0}; float[] falseCnts = {0, 0, 0};
      for (NumericMentionExpression expression : rawDataset) {
        int size = expression.expression.arguments().size();
        if (expression.label.get() > 0.5)
          trueCnts[size-1]++;
        else
          falseCnts[size-1]++;
      }
      for(int i = 0; i < 3; i++) {
        scale[i] = Math.min(1, (1+trueCnts[i]) / (1+falseCnts[i]));
      }
    }

    rawDataset.stream().forEach(e -> {
      int size = e.expression.arguments().size();
      // stochastically add to the dataset.
      float weight = e.label.get() > 0.5 ? 1 : scale[size-1];
//      if (rng.nextDouble() < weight)
      dataset.add(f.featurizeConfidence(e), weight);
    });

    dataset.applyFeatureCountThreshold(10);
    dataset.randomize(42);
    log(dataset.numDatumsPerLabel());

    return run(factory, dataset);
  }

  public void explainOutput(LinearRegressor<String> classifier, Featurizer featurizer, List<NumericMentionExpression> rawDataset) {
    PrintWriter outputWriter = new PrintWriter(System.out);

    Map<NumericMention, List<NumericMentionExpression>> rawDatasetByMention = rawDataset.stream()
        .collect(Collectors.groupingBy(k -> k.mention));

    for(Map.Entry<NumericMention, List<NumericMentionExpression>> entry : rawDatasetByMention.entrySet()) {
      NumericMention mention = entry.getKey();
      outputWriter.write(String.format("S: %s\n", mention.sentence.get()));

      entry.getValue().stream()
          .filter(e -> Math.abs(Math.log(mention.normalized_value/e.expression.value)) <= Math.log(10.1)) // Anything in range of 100.
          .map(e -> Pair.makePair(e, classifier.valueOf(featurizer.featurizeConfidence(e))))
          .sorted((p1, p2) -> Double.compare(p2.second, p1.second))
          .collect(Collectors.toList())
          // Print output.
          .forEach(p -> {
            outputWriter.write(String.format("\t%.2f\t*%s\t%s {\n", p.second, p.first.label.map(f -> String.format("%.2f", f)).orElse("?"), p.first.expression));
            classifier.justificationOf(featurizer.featurizeConfidence(p.first), outputWriter);
            outputWriter.write("}\n");
          });
    }

    outputWriter.close();
  }

  @Override
  public void run() {
    List<NumericMentionExpression> rawDataset;
    try {
      embeddings = (embFile.length() > 0) ? WordEmbedding.loadFromFile(embFile) : WordEmbedding.empty();
      rawDataset = NumericMentionExpression.readFromTSV(input);
      if(subset > 0)
        rawDataset = rawDataset.stream().filter(me -> me.expression.arguments().size() == subset).collect(Collectors.toList());
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }

    LinearRegressionFactory<String> factory = new LinearRegressionFactory<>();
    Featurizer f = new Featurizer(embeddings, featureTemplates);

    LinearRegressor<String> classifier = run(factory, f, rawDataset);
    if (!Objects.equals(output, ""))
      LinearRegressor.writeClassifier(classifier, output);
    explainOutput(classifier, f, rawDataset);
  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(LearnRankingConfidence.class, args);
    new LearnRankingConfidence().run();
  }
}
