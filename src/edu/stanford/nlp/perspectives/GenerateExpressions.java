package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.nlp.util.logging.Redwood.Util.logf;
import static edu.stanford.nlp.util.logging.Redwood.Util.startTrack;
import static edu.stanford.nlp.util.logging.Redwood.endTrack;
import static edu.stanford.nlp.util.logging.Redwood.log;

/**
 * Created by chaganty on 2/22/16.
 */
public class GenerateExpressions implements Runnable {

  @ArgumentParser.Option(name="input", gloss="CSV file input with fields [id, id, ignore, source, target]")
  public static InputStream input = System.in;

  @ArgumentParser.Option(name="nSteps", gloss="number of steps to take")
  public static int nSteps = 100;

  public static final int N_FIELDS = 5;

  public List<Pair<String, String>> getBiCorpus(InputStream in) {
    RecordIterator rit = new RecordIterator(new InputStreamReader(in), N_FIELDS, true, "\t");
    List<Pair<String, String>> lst = new ArrayList<>();

    // skip header
    List<String> header = rit.next();
    assert header.get(3).equals("simple_prompt");
    assert header.get(4).equals("description");

    while(rit.hasNext()) {
      List<String> entries = rit.next();
      lst.add(Pair.makePair(entries.get(3), entries.get(4)));
    }

    return lst;
  }

  @Override
  public void run() {
    startTrack("loading data");
    List<Pair<String, String>> biCorpus = getBiCorpus(input);
    logf("loaded %d sentences", biCorpus.size());
    endTrack("loading data");

    startTrack("training model");
    IBMModel1 model = IBMModel1.fromBiCorpus(biCorpus);
    for(int i = 0; i < nSteps; i++) {
      log("iteration " + i);
      model.doEMStep(biCorpus);
    }
    endTrack("training model");

    // Print output
    for(Pair<String, String> p : biCorpus) {
      startTrack(p.first);
      log("expected: " + p.second);
      for(int i = 0; i < 5; i++) {
        log("generated: " + model.generate(p.first));
      }
      endTrack(p.first);
    }


  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(GenerateExpressions.class, args);

    new GenerateExpressions().run();
  }
}
