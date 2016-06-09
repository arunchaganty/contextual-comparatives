package edu.stanford.nlp.perspectives;

    import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.kbp.common.PostgresIOUtils;
    import edu.stanford.nlp.util.ArgumentParser;
    import edu.stanford.nlp.util.StringUtils;

    import java.io.*;
import java.util.ArrayList;
import java.util.List;
    import java.util.Properties;
    import java.util.stream.Collectors;
import java.util.stream.IntStream;

    import static edu.stanford.nlp.util.logging.Redwood.Util.endTrack;
    import static edu.stanford.nlp.util.logging.Redwood.Util.log;
    import static edu.stanford.nlp.util.logging.Redwood.Util.startTrack;
    import static edu.stanford.nlp.util.logging.Redwood.logf;

/**
 * Script returns a comparison score.
 */
public class GenerateComparisons implements Runnable {

  @ArgumentParser.Option(gloss="Path to TSV file with numeric data comparisons")
  public static String numericData = "";

  @ArgumentParser.Option(gloss="Path to TSV file with numeric data comparisons")
  public static String wordEmbeddings = "";

  @ArgumentParser.Option(gloss="Path to TSV file with unit conversions")
  public static String unitConversions = "";

  @ArgumentParser.Option(gloss="Path to output TSV file ('-' for stdout)")
  public static String output = "-";

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

  Writer getWriter(String path) {
    try {
      if (path.equals("-")) {
        return new OutputStreamWriter(System.out);
      } else {
        return new FileWriter(path);
      }
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }

  public String toOutput(NumericMention mention, Expr expr) {
    double multiplier = mention.value / expr.value;
    List<Integer> ids = expr.arguments().stream().map(a -> a.id.orElse(-1)).collect(Collectors.toList());
    // output: multiplier, {data_id_1}, value, unit
    return String.format("%f\t%s\t%f\t%s\n",
          multiplier,
          PostgresIOUtils.writeArray(ids),
          expr.value,
          expr.unit);
  }

  @Override
  public void run() {
    Writer outputWriter = getWriter(output);

    UnitDatabase unitTuples = UnitDatabase.readFromTSV(unitConversions);
    log("Read units: " + unitTuples.size());
    TupleDatabase data = TupleDatabase.compile(readData(numericData), unitTuples);
    log("Read data: " + data.getNumEdges());
    Util.WordEmbedding emb = Util.WordEmbedding.loadFromFile(wordEmbeddings);
    log("Read emb: " + emb.size());

    GraphSearcher graphSearcher = new GraphSearcher(data, emb);

    List<Double> values = IntStream.range(-9, 10).mapToObj(i -> Math.pow(10, i)).collect(Collectors.toList());
    // For every unit and every order of magnitude from 1e-10 to 1e10, produce outputs.
    for(Unit u : data.getAllVertices()) {
      for(Double value : values) {
        final NumericMention m = NumericMention.of(value, u.toString());
        List<Expr> exprs = graphSearcher.generate(m);
        String name = value + " " + u;
        startTrack(name);
        graphSearcher.reorder(m, exprs)
            .forEach(e -> {
              try {
                logf("%.2f %s", e.second, e.first.evaluate().comparisonNP(m));
                logf("%.2f %s", e.second, e.first);
                outputWriter.append(toOutput(m, e.first));
              } catch (IOException e1) {
                throw new RuntimeIOException(e1);
              }
            });
        endTrack(name);
      }
    }
    try {
      outputWriter.close();
    } catch (IOException e1) {
      throw new RuntimeIOException(e1);
    }
  }

  public static void main(String[] args) {
    Properties props = StringUtils.argsToProperties(args);
    ArgumentParser.fillOptions(new Class[]{GenerateComparisons.class}, props);

    new GenerateComparisons().run();
  }
}
