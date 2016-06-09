package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.Util;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArrayUtils;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An implementation of IBM Model 1
 */
public class IBMModel1 {
  // Parameters

  // length model
//  Map<Integer, Counter<Integer>> lengthMap;
  // alignment model -- currently uniform.
//  Map<Pair<Integer, Integer>, Counter<Integer>> alignmentMap;
  // translation model -- Map<word, Counter<words>>
  Map<String, Counter<String>> translationMap;
  Map<Integer, Counter<Integer>> lengthMap;
  Random rng = new Random(42);

  public IBMModel1(Map<Integer, Counter<Integer>> lengthMap,
                   Map<String, Counter<String>> translationMap) {
    this.lengthMap = lengthMap;
    this.translationMap = translationMap;
  }

  protected String sampleWord(String word) {
    return Util.sample(rng, translationMap.get(word));
  }

  // Alignments are a map from j \in J to i \in I a[j] -> i
  protected int[] sampleAlignment(int I, int J) {
    int[] alignment = new int[J];

    // -- initialize distribution
    Counter<Integer> target = new ClassicCounter<>();
    for(int i = 0; i < I; i++) target.incrementCount(i);
    Util.normalize(target);

    // -- set alignment
    for(int j = 0; j < J; j++) {
      alignment[j] = Util.sample(rng, target);
    }

    return alignment;
  }

  // Length
  protected int sampleLength(int I) {
    return Util.sample(rng, lengthMap.get(I));
  }

  protected int[] bestAlignment(String[] input, String[] output) {
    int I = input.length;
    int J = output.length;

    int[] alignment = new int[J];
    // a_j = argmax_{i} t(f_j | e_i)
    for(int j = 0; j < J; j++) {
      double score = 0;
      int argmax_i = 0;
      for(int i = 0; i < I; i++) {
        double score_ = translationMap.get(input[i]).getCount(output[j]);
        if (score_ > score) {
          argmax_i = i;
          score = score_;
        }
      }
      alignment[j] = argmax_i;
    }
    return alignment;
  }


  public static String[] tokenize(String str) {
    return str.split("\\s+");
  }

  /**
   * Train model using bicorpus
   * @param biCorpus
   */
  void doEMStep(List<Pair<String, String>> biCorpus) {
    Map<String, Counter<String>> counts = newTranslationMap();

    // E-step, compute expected counts
    for(Pair<String, String> pair : biCorpus) {
      String[] input = tokenize(pair.first);
      String[] output = tokenize(pair.second);

      // Get expected alignment
      updateCounts(counts, input, output);
    }
    // normalize
    normalize(counts);

    this.translationMap = counts;
  }

  protected static <T,U> void normalize(Map<T, Counter<U>> counts) {
    for(T key : counts.keySet()) {
      Util.normalize(counts.get(key));
    }
  }


  // -- creates a copy of the original translation map with zeroed out counters
  protected Map<String, Counter<String>> newTranslationMap() {
    Map<String, Counter<String>> newMap = new HashMap<>();
    for(String key : translationMap.keySet()) {
      newMap.put(key, new ClassicCounter<>());
    }
    return newMap;
  }

  protected void updateCounts(Map<String, Counter<String>> counts, String[] input, String[] output) {
    int I = input.length;
    int J = output.length;

    for(int j = 0; j < J; j++) {
      double[] scores = new double[I];
      for(int i = 0; i < I; i++) {
        scores[i] = translationMap.get(input[i]).getCount(output[j]);
      }
      scores = ArrayUtils.normalize(scores);
      for(int i = 0; i < I; i++) {
        counts.get(input[i]).incrementCount(output[j], scores[i]);
      }
    }
  }

  public List<String> generate(String[] input) {
    int I = input.length;
    int J = sampleLength(I);
    int[] alignment = sampleAlignment(I, J);
    List<String> output = new ArrayList<>(J);
    for (int j = 0; j < J; j++) {
      int w_aj = alignment[j];
      output.add(sampleWord(input[w_aj]));
    }

    return output;
  }
  public String generate(String input) {
    return String.join(" ", generate(tokenize(input)));
  }

  public static IBMModel1 fromBiCorpus(List<Pair<String, String>> biCorpus) {
    Redwood.hideChannelsEverywhere();
    // Initialize map.
    Set<String> fromLexicon = biCorpus.stream().flatMap(p -> Arrays.stream(tokenize(p.first))).collect(Collectors.toSet());
    int maxLength = biCorpus.stream().map(p -> tokenize(p.first).length).max((x,y) -> x - y).orElse(1);
//    Set<String> toLexicon = biCorpus.stream().flatMap(p -> Arrays.stream(tokenize(p.second))).collect(Collectors.toSet());

    Map<String, Counter<String>> translationMap = fromLexicon.stream().collect(Collectors.toMap(k -> k, k -> new ClassicCounter<>()));
    Map<Integer, Counter<Integer>> lengthMap = IntStream.range(1,maxLength+1).boxed().collect(Collectors.toMap(k -> k, k -> new ClassicCounter<>()));
    for(Pair<String, String> pair : biCorpus) {
      for(String from : tokenize(pair.first)) {
        for(String to : tokenize(pair.second)) {
          translationMap.get(from).incrementCount(to);
        }
      }

      lengthMap.get(tokenize(pair.first).length).incrementCount(tokenize(pair.second).length);
    }
    normalize(translationMap);
    normalize(lengthMap);

    return new IBMModel1(lengthMap, translationMap);
  }
}
