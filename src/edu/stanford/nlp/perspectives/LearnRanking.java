package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util.WordEmbedding;
import edu.stanford.nlp.classify.*;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.ConfusionMatrix;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.Triple;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Ranking function for numeric mention expressions
 */
public class LearnRanking implements Runnable {
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

  @ArgumentParser.Option(name="outputPredictions", gloss="Save serialized model")
  public static OutputStream outputPredictionsStream = System.out;

  public OutputStreamWriter outputPredictions;

  public final Random rng = new Random(42);

  public void printOutput(LinearClassifier<Boolean, String> classifier, Featurizer f, List<NumericMentionExpression> rawDataset) throws IOException {
    final WeightedRVFDataset<Boolean, String> dataset = new WeightedRVFDataset<>();

    // Featurize dataset
    rawDataset.stream().forEach(e -> {
      dataset.add(f.featurize(e));
    });
    // Classify output.
    outputPredictions.write("mention_id\tresponse_id\tgold\tguess\n");
    for(int i = 0; i < rawDataset.size(); i++) {
      NumericMentionExpression expression = rawDataset.get(i);
      Datum<Boolean, String> datum = dataset.getDatum(i);
      boolean useful = classifier.classOf(datum);
      outputPredictions.write(String.format("%d\t%d\t%d\t%d\n", expression.mention_id, expression.response_id, (expression.label.get() > 0.5) ? 1 : 0, useful ? 1 : 0));
    }
  }

  public LinearClassifier<Boolean, String> run(final LinearClassifierFactory<Boolean, String> factory, final RVFDataset<Boolean, String> dataset) {
    CrossValidator<Boolean, String> cv = new CrossValidator<>(dataset, 10);
    double testAcc = cv.computeAverage(t -> {
      GeneralDataset<Boolean, String> train = t.first;
      GeneralDataset<Boolean, String> test = t.second;

      LinearClassifier<Boolean, String> classifier = factory.trainClassifier(train);
      ConfusionMatrix<Boolean> trainConfusion = new ConfusionMatrix<>();
      for(Datum<Boolean, String> datum : train) {
        trainConfusion.add(classifier.classOf(datum), datum.label());
      }

      ConfusionMatrix<Boolean> testConfusion = new ConfusionMatrix<>();
      for(Datum<Boolean, String> datum : test) {
        testConfusion.add(classifier.classOf(datum), datum.label());
      }

      t.third.state = Triple.makeTriple(classifier, trainConfusion, testConfusion);
      return testConfusion.getContingency(true).f1();
    });

    Pair<ConfusionMatrix<Boolean>, ConfusionMatrix<Boolean>> pair = cv.getSavedStates().stream()
        .map(o_ -> {
          Triple<?, ConfusionMatrix<Boolean>, ConfusionMatrix<Boolean>> o = (Triple<?, ConfusionMatrix<Boolean>, ConfusionMatrix<Boolean>>) o_;
          return Pair.makePair(o.second, o.third);
        })
        .reduce(Pair.makePair(new ConfusionMatrix<>(), new ConfusionMatrix<>()), (acc, o) -> {
          acc.first.addAll(o.first);
          acc.second.addAll(o.second);
          return acc;
        });

    Counter<String> features = cv.getSavedStates().stream()
        .map(o_ -> ((Triple<LinearClassifier<Boolean, String>, ?, ?>) o_).first)
        .map(c -> c.weightsAsMapOfCounters().get(true))
        .reduce(new ClassicCounter<>(), (acc, o) -> {
          acc.addAll(o);
          return acc;
        });
    features = Counters.scale(features, 1./cv.getKFold());

    // TODO(chaganty): compute P/R/F1
    log("Features: " + featureTemplates);
    log(String.format("Cross validated train A/P/R/F1: %.3f/%.3f/%.3f/%.3f",
        pair.first.getContingency(true).accuracy(),
        pair.first.getContingency(true).precision(),
        pair.first.getContingency(true).recall(),
        pair.first.getContingency(true).f1()));
    log(String.format("Cross validated test A/P/R/F1: %.3f/%.3f/%.3f/%.3f",
        pair.second.getContingency(true).accuracy(),
        pair.second.getContingency(true).precision(),
        pair.second.getContingency(true).recall(),
        pair.second.getContingency(true).f1()));
    System.out.println(String.join("\t", Arrays.asList(
        "features",
        "train-acc", "train-prec", "train-rec", "train-f1",
        "test-acc", "test-prec", "test-rec", "test-f1"
    )));
    System.out.println(String.join("\t", Arrays.asList(
        featureTemplates,
        String.format("%.3f", pair.first.getContingency(true).accuracy()),
        String.format("%.3f", pair.first.getContingency(true).precision()),
        String.format("%.3f", pair.first.getContingency(true).recall()),
        String.format("%.3f", pair.first.getContingency(true).f1()),
        String.format("%.3f", pair.second.getContingency(true).accuracy()),
        String.format("%.3f", pair.second.getContingency(true).precision()),
        String.format("%.3f", pair.second.getContingency(true).recall()),
        String.format("%.3f", pair.second.getContingency(true).f1())
    )));
    if(outputFeatures)
      outputFeatures(features);

    // Create a classifier with these features.
    Counter<Pair<String, Boolean>> classifierFeatures = new ClassicCounter<>(
    features.entrySet().stream().flatMap(f ->
        Stream.of(
            Pair.makePair(Pair.makePair(f.getKey(),true), f.getValue()),
            Pair.makePair(Pair.makePair(f.getKey(),false), - f.getValue())
        ))
        .collect(Collectors.toMap(k -> k.first, k -> k.second)));

    // Actually train on the entire dataset.
    //
    return factory.trainClassifier(dataset);
  }

  public LinearClassifier<Boolean, String> run(final LinearClassifierFactory<Boolean, String> factory, Featurizer f, List<NumericMentionExpression> rawDataset) {
    final WeightedRVFDataset<Boolean, String> dataset = new WeightedRVFDataset<>();

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
      dataset.add(f.featurize(e), weight);
  });

    dataset.applyFeatureCountThreshold(12);
    dataset.randomize(42);
    log(dataset.numDatumsPerLabel());

    return run(factory, dataset);
  }

  public void outputFeatures(Counter<String> weights) {
//    System.out.println(classifier.toBiggestWeightFeaturesString(true, 10, true));
//    System.out.println(classifier.toHistogramString();

//    Counter<String> weights = classifier.weightsAsMapOfCounters().get(true);
    weights.entrySet().stream().sorted((e1, e2) -> Double.compare(Math.abs(e1.getValue()), Math.abs(e2.getValue())))
        .forEach(e -> System.out.println(String.format("%s\t%.3f", e.getKey(), e.getValue())));
  }

  public void explainFeatures(LinearClassifier<Boolean, String> classifier, Featurizer featurizer, List<NumericMentionExpression> rawDataset) {
    PrintWriter outputWriter = new PrintWriter(System.out);

    Map<NumericMention, List<NumericMentionExpression>> rawDatasetByMention = rawDataset.stream()
        .collect(Collectors.groupingBy(k -> k.mention));

    for(Map.Entry<NumericMention, List<NumericMentionExpression>> entry : rawDatasetByMention.entrySet()) {
      NumericMention mention = entry.getKey();
      outputWriter.write(String.format("S: %s\n", mention.sentence.get()));

     entry.getValue().stream()
          .filter(e -> Math.abs(Math.log(mention.normalized_value/e.expression.value)) <= Math.log(10.1)) // Anything in range of 100.
          .map(e -> Pair.makePair(e, classifier.probabilityOf(featurizer.featurize(e)).getCount(true)) )
          .sorted((p1, p2) -> Double.compare(p2.second, p1.second))
          .collect(Collectors.toList())
      // Print output.
      .forEach(p -> {
        outputWriter.write(String.format("\t%.2f\t%s\t%s {\n", p.second, p.first.label.map(d -> d > 0.5 ? "T" : "F").orElse("?"), p.first.expression));
        classifier.justificationOf(featurizer.featurize(p.first), outputWriter);
        outputWriter.write("}\n");
      });
    }

    outputWriter.close();
  }

  @Override
  public void run() {
    outputPredictions = new OutputStreamWriter(outputPredictionsStream);

    List<NumericMentionExpression> rawDataset;
    try {
      embeddings = (embFile.length() > 0) ? WordEmbedding.loadFromFile(embFile) : WordEmbedding.empty();
      rawDataset = NumericMentionExpression.readFromTSV(input);
      if(subset > 0)
        rawDataset = rawDataset.stream().filter(me -> me.expression.arguments().size() == subset).collect(Collectors.toList());
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    LinearClassifierFactory<Boolean, String> factory = new LinearClassifierFactory<>();
    Featurizer f = new Featurizer(embeddings, featureTemplates);

    LinearClassifier<Boolean, String> classifier = run(factory, f, rawDataset);
    if (!Objects.equals(output, ""))
      LinearClassifier.writeClassifier(classifier, output);
    explainFeatures(classifier, f, rawDataset);

    try {
      printOutput(classifier, f, rawDataset);
      outputPredictions.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(LearnRanking.class, args);
    new LearnRanking().run();
  }
}
