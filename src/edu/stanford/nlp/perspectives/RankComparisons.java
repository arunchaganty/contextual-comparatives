package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.arguments.Util.WordEmbedding;
import edu.stanford.nlp.classify.LinearClassifier;
import edu.stanford.nlp.classify.LinearRegressor;
import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.kbp.common.PostgresIOUtils;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.TSVSentenceIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * Script returns a comparison score.
 */
public class RankComparisons implements Runnable {
  @ArgumentParser.Option(gloss="Path to input TSV file ('-' for stdin)", required=true)
  public static String input = "";

  @ArgumentParser.Option(name="tuples", gloss="Path to TSV file with numeric data comparisons", required=true)
  public static String numericData = "";

  @ArgumentParser.Option(name="embeddings", gloss="Path to TSV file with numeric data comparisons", required=true)
  public static String wordEmbeddings = "";

  @ArgumentParser.Option(name="units", gloss="Path to TSV file with unit conversions", required=true)
  public static String unitConversions = "";

  public enum Mode {
    BASELINE, // Get the best fact of length 1.
    ENUMERATION, // Get all the facts
    CLASSIFIER, // Use the classifier
  }

  @ArgumentParser.Option(name="mode", gloss="baseline, enumeration or classifier")
  public static Mode mode = Mode.CLASSIFIER;

  @ArgumentParser.Option(name="isClassifier", gloss="Classification or regression?", required=true)
  public static boolean isClassifier = true;

  @ArgumentParser.Option(name="model", gloss="Path to a ranking classifier", required=true)
  public static String modelPath = "";
  private LinearClassifier<Boolean, String> classifier;
  private LinearRegressor<String> regressor;

  @ArgumentParser.Option(name="features", gloss="Features to use", required=true)
  public static String featureTemplates = "*";
  private Featurizer featurizer;

  @ArgumentParser.Option(name="outputIds", gloss="Output the mention, expression ids")
  public static boolean outputIds = false;

  @ArgumentParser.Option(gloss="Path to output TSV file ('-' for stdout)")
  public static OutputStream output = System.out;
  PrintWriter outputWriter;


  public List<NumericMention> readMentionsLazy(Reader inputReader) {
    List<NumericMention> mentions = new ArrayList<>();
    RecordIterator rit = new RecordIterator(inputReader, Util.fields.size(), true, "\t");
    TSVSentenceIterator it = new TSVSentenceIterator(rit, Util.fields);

    NumericMentionAnnotator ann = new NumericMentionAnnotator();

    while(it.hasNext()) {
      Sentence sentence = it.next();
      mentions.addAll(ann.annotate(sentence));
    }
    return mentions;
  }
  public List<NumericTuple> readData(String filename) {
    List<NumericTuple> data = new ArrayList<>();
    RecordIterator rit;
    try {
      rit = new RecordIterator(filename, "\t");
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    while(rit.hasNext()) {
      List<String> entries = rit.next();
      NumericTuple tuple = new NumericTuple(
          Integer.parseInt(entries.get(0)),    //id
          entries.get(1),                     // name
          entries.get(2),                     // relation
          Double.parseDouble(entries.get(3)), // value
          entries.get(4)                     // unit
      );
      data.add(tuple);
    }
    return data;
  }

  List<Pair<NumericMentionExpression, Double>> processMention(Searcher searcher, final NumericMention mention) {
    startTrack(mention.sentence.get().toString());
    List<Expr> exprs = searcher.generate(mention);
    log(mention);
    logf("Found %d exprs", exprs.size());

    if (mode == Mode.CLASSIFIER)
      assert isClassifier ? (classifier != null) : (regressor != null);

    // Featurize all the mentions.
    List<Pair<NumericMentionExpression, Double>> lst = exprs.stream()
        .filter(e -> (mode == Mode.BASELINE) ? e.arguments().size() == 1
            : Math.abs(Math.log(mention.normalized_value/e.value)) <= Math.log(10.1)) // Anything in range of 100. if not baseline.
        .map(e -> NumericMentionExpression.of(mention, e))
        .map(e -> {
          switch(mode) {
            case CLASSIFIER:
              return Pair.makePair(e,
              isClassifier ? classifier.probabilityOf(featurizer.featurize(e)).getCount(true)
                           : regressor.valueOf(featurizer.featurizeConfidence(e)));
            case ENUMERATION:
              return Pair.makePair(e, 0.); // no sorting.
            case BASELINE:
              return Pair.makePair(e, -Math.abs(Math.log(e.expression_multiplier))); // score is numeric distance.
            default:
              throw new RuntimeException();
          }})
        .sorted((p1, p2) -> Double.compare(p2.second, p1.second))
        .collect(Collectors.toList());

    if (!outputIds) {
      outputWriter.write(String.format("S: %s\n", mention.sentence.get()));
      // Print output.
      lst.forEach(p -> {
        outputWriter.write(String.format("\t%.2f\t%s {\n", p.second, p.first.expression));
        if(isClassifier)
          classifier.justificationOf(featurizer.featurize(p.first), outputWriter);
        else
          regressor.justificationOf(featurizer.featurizeConfidence(p.first), outputWriter);
        outputWriter.write("}\n");
      });
    }
    endTrack(mention.sentence.get().toString());

    return lst;
  }

  void runConsole(Searcher searcher) {
    Console console = System.console();
    Pattern number = Pattern.compile("\\b([0-9]+,?[0-9]*\\.?[0-9]*)\\s+([a-zA-Z]+\\s*(per [a-zA-Z]+)*)");
    while (true) {
      String line = console.readLine("cc> ");
      // accept input as (number unit);
      Matcher m = number.matcher(line);
      if (m.find()) {
        double val = Double.parseDouble(m.group(1));
        String unit = m.group(2);
        NumericMention mention = NumericMention.of(val, unit, line);
        mention = searcher.getTdb().unitConversion.standardizeUnits(mention);
        processMention(searcher, mention);
      } else {
        console.printf("invalid string.\n");
      }
      if (line.equals("quit")) break;
    }
  }

  @Override
  public void run() {
    // Create an input reader
//    Reader inputReader = new InputStreamReader(input);
    outputWriter = new PrintWriter(output);

    UnitDatabase unitTuples = UnitDatabase.readFromTSV(unitConversions);
    log("Read units: " + unitTuples.size());
    TupleDatabase data = TupleDatabase.compile(readData(numericData), unitTuples);
    log("Read data: " + data.getNumEdges());
    WordEmbedding emb = (wordEmbeddings.length() > 0) ? WordEmbedding.loadFromFile(wordEmbeddings) : WordEmbedding.empty();
    log("Read emb: " + emb.size());

    if (mode == Mode.CLASSIFIER) {
      if (isClassifier)
        classifier = LinearClassifier.readClassifier(modelPath);
      else
        regressor = LinearRegressor.readRegressor(modelPath);
    }

    featurizer = new Featurizer(emb, featureTemplates);

    GraphSearcher graphSearcher = new GraphSearcher(data, emb);
    List<NumericMention> mentions = null;
    try {
      mentions = Util.readMentions(input);
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    log("Read mentions: " + mentions.size());
    List<Pair<NumericMentionExpression, Double>> lst = mentions.stream()
        .map(m -> graphSearcher.getTdb().unitConversion.standardizeUnits(m))
        .flatMap(m -> processMention(graphSearcher, m).stream().limit(3)) // Only keep the top 3 predictions.
        .collect(Collectors.toList());

    if (outputIds) {
      // Output the mentions
      outputWriter.append(String.join("\t", "mention_id", "multiplier", "arguments", "value", "unit", "score")).append('\n');
      for (Pair<NumericMentionExpression, Double> pair : lst) {
        NumericMentionExpression me = pair.first;
        double score = pair.second;
        List<Integer> ids = me.expression.arguments().stream().map(a -> a.id.orElse(-1)).collect(Collectors.toList());
        // output: multiplier, {data_id_1}, value, unit
        outputWriter.append(String.join("\t", Integer.toString(me.mention.id.orElse(-1)),
            Double.toString(me.expression_multiplier),
            PostgresIOUtils.writeArray(ids),
            Double.toString(me.expression_value),
            me.expression_unit.toString(),
            String.format("%.4f", score))).append('\n');
      }
    }

    outputWriter.close();
  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(RankComparisons.class, args);
    new RankComparisons().run();
  }
}
