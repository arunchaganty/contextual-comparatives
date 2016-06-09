package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.ConfusionMatrix;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * Prints statistics about the ranking dataset.
 */
public class RankDatasetAnalysis implements Runnable {
  @ArgumentParser.Option(name="input", gloss="TSV file with input", required = true)
  public static String input;
  @ArgumentParser.Option(name="embeddings", gloss="TSV file with wv embeddings", required = true)
  public static String embFile;
  Util.WordEmbedding embeddings;

  @ArgumentParser.Option(name="features", gloss="comma separated list of features to use")
  public static String featureTemplates = "bias";

  @ArgumentParser.Option(name="outputFeatures", gloss="Output features")
  public static boolean outputFeatures = false;

  protected double semanticSimilarity(Counter<String> features, String prefix, Sentence str1, Sentence str2) {
    return semanticSimilarity(features, prefix, str1.toString(), str2.toString());
//    for(String lemma1 : str1.lemmas()) {
//      for(String lemma2 : str2.lemmas()) {
//        features.incrementCount(prefix+"-" + lemma1.toLowerCase() + "-" + lemma2.toLowerCase());
//      }
//    }
  }
  protected double semanticSimilarity(Counter<String> features, String prefix, String str1, String str2) {
    double sim = Util.WordEmbedding.dotProduct(embeddings.get(str1), embeddings.get(str2));
    features.incrementCount(prefix + "semantic-similarity", sim);
    return sim;
  }

  @Override
  public void run() {
    List<NumericMentionExpression> rawDataset;
//    List<NumericMentionExpression> rawDataset1;
//    List<NumericMentionExpression> rawDataset2;
//    List<NumericMentionExpression> rawDataset3;
    try {
      embeddings = (embFile.length() > 0) ? Util.WordEmbedding.loadFromFile(embFile) : Util.WordEmbedding.empty();
      rawDataset = NumericMentionExpression.readFromTSV(input);
//      rawDataset1 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 1).collect(Collectors.toList());
//      rawDataset2 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 2).collect(Collectors.toList());
//      rawDataset3 = rawDataset.stream().filter(e -> e.expression.arguments().size() == 3).collect(Collectors.toList());
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }


    Map<Sentence, List<NumericMentionExpression>> options = rawDataset.stream().collect(Collectors.groupingBy(e -> e.mention_sentence));
    // How many expressions have any valid output?
    log("Unique sentences \t " + options.size());

    double validOutputFraction = options.values().stream()
        .collect(Collectors.averagingDouble(elem -> elem.stream().anyMatch(e -> e.label.get() > 0.5) ? 1.0 : 0.0));
    log("expressions with valid output \t " + validOutputFraction);

    // How many expressions have a valid with 1, 2, 3 expressions?

    double validOutputFraction1 = options.values().stream()
        .collect(Collectors.averagingDouble(elem -> elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 1) ? 1.0 : 0.0));
    double validOutputFraction2 = options.values().stream()
        .collect(Collectors.averagingDouble(elem -> elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 2) ? 1.0 : 0.0));
    double validOutputFraction3 = options.values().stream()
        .collect(Collectors.averagingDouble(elem -> elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 3) ? 1.0 : 0.0));
    log("expressions with valid output 1 \t " + validOutputFraction1);
    log("expressions with valid output 2 \t " + validOutputFraction2);
    log("expressions with valid output 3 \t " + validOutputFraction3);

    double validOutputFractiongt1 = options.values().stream()
        .collect(Collectors.averagingDouble(elem ->
            elem.stream().noneMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 1) &&
            elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() > 1) ? 1.0 : 0.0));
    log("expressions with valid output only >1 \t " + validOutputFractiongt1);

    // How many expressions have a valid 1 and >1 expression?
    double validOutputFraction12 = options.values().stream()
        .filter(elem -> elem.stream().anyMatch(e -> e.expression.arguments().size() == 1) &&
            elem.stream().anyMatch(e -> e.expression.arguments().size() > 1))
        .collect(Collectors.averagingDouble(elem ->
            elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 1) &&
            elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() > 1)
            ? 1.0 : 0.0));
    log("expressions with valid output 1 & >1 \t " + validOutputFraction12);

    // How many expressions have a invalid 1 and valid >1 expression?
    double validOutputFraction2gt1 = options.values().stream()
        .filter(elem -> elem.stream().anyMatch(e -> e.expression.arguments().size() == 1) &&
            elem.stream().anyMatch(e -> e.expression.arguments().size() > 1))
        .collect(Collectors.averagingDouble(elem ->
            elem.stream().noneMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 1) &&
                elem.stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() > 1)
                ? 1.0 : 0.0));
    double validOutputFraction1gt2 = 1 - validOutputFraction2gt1;
    log("expressions with valid 1 and invalid >1 \t " + validOutputFraction1gt2);
    log("expressions with valid >1 and invalid 1 \t " + validOutputFraction2gt1);

    options.entrySet().stream()
        .filter(elem -> elem.getValue().stream().anyMatch(e -> e.expression.arguments().size() == 1) &&
            elem.getValue().stream().anyMatch(e -> e.expression.arguments().size() > 1))
        .filter(elem -> elem.getValue().stream().noneMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() == 1) &&
        elem.getValue().stream().anyMatch(e -> e.label.get() > 0.5 && e.expression.arguments().size() > 1))
        .forEach(e -> {
              Sentence s = e.getKey();
              startTrack(s.toString());
          e.getValue().stream().sorted((x1, x2) ->
              (x1.label.get() == x2.label.get()) ? (Integer.compare(x1.expression.arguments().size(), x2.expression.arguments().size())) :
                  Double.compare(x2.label.get(), x1.label.get())
          ).forEach(x ->
                log(x.response + " " + x.label));
              endTrack(s.toString());
            });

    // Print out vector overlap with tuples..

//
//    List<String> keys = rawDataset.stream().flatMap(d -> d.expression.arguments().stream()).map(x -> x.subj).distinct().sorted().collect(Collectors.toList());
//    List<String> contexts = rawDataset.stream().map(d -> d.mention_sentence.toString()).distinct().sorted().collect(Collectors.toList());
//    for(String c : contexts) {
//      log("==MENTION: " + c);
//      double[] cv = embeddings.get(c);
//      Map<String, Double> matches = keys.stream().collect(Collectors.toMap(k -> k, k -> Util.WordEmbedding.dotProduct(cv, embeddings.get(k))));
//      matches.entrySet().stream().sorted((e1,e2) -> Double.compare(e2.getValue(), e1.getValue())).forEach(e -> log(e));
//    }



//    // Compute similarity between all tuples.
//    Map<String, Map<String, Double>> matrix = new HashMap<>();
//    for(String t : keys) {
//      matrix.put(t, new HashMap<String, Double>());
//    }
//    double max = Double.MIN_VALUE;
//    double min = Double.MAX_VALUE;
//    for(String k : keys) {
//      double[] v1 = embeddings.get(k);
//      for (String k_ : keys) {
//        double[] v2 = embeddings.get(k_);
//        double sim = Util.WordEmbedding.dotProduct(v1, v2);
//        matrix.get(k).put(k_, sim);
//        max = Math.max(max, sim);
//        min = Math.min(min, sim);
//      }
//    }
//    // Rescale data.
//    for(String k : keys) {
//      for (String k_ : keys) {
//        double sim = matrix.get(k).get(k_);
//        sim = 2.0 * (sim - min)/(max - min) - 1.0;
//        matrix.get(k).put(k_, sim);
//      }
//    }
//
//    System.out.println(String.join("\t", keys));
//    for(int i = 0; i < keys.size(); i++) {
//      System.out.print(keys.get(i) + "\t");
//      Map<String, Double> row = matrix.get(keys.get(i));
//      System.out.println(String.join("\t", keys.stream().map(k -> String.format("%.2f", row.get(k))).collect(Collectors.toList())));
//    }

  }


  public static void main(String[] args) {
    ArgumentParser.fillOptions(RankDatasetAnalysis.class, args);
    new RankDatasetAnalysis().run();
  }
}
