package edu.stanford.nlp.util;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.base.*;
import com.google.common.collect.*;
import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.simple.Sentence;

import static edu.stanford.nlp.util.logging.Redwood.log;

/**
 * Stolen from Jonathan Clark's MT evaluation code.
 * https://github.com/jhclark/multeval
 */
// this is a reimplementation of NIST's MT eval 13b
// minus the problematic pre-processing and SGML handling
public class JBLEU implements  Runnable {
  private static class HashUtil {
    /* This method was written by Doug Lea with assistance from members of JCP
     * JSR-166 Expert Group and released to the public domain, as explained at
     * http://creativecommons.org/licenses/publicdomain As of 2010/06/11, this
     * method is identical to the (package private) hash method in OpenJDK 7's
     * java.util.HashMap class. It was in turn lifted from Google Guava's Hashing
     * class. */
    static int smear(int hashCode) {
      hashCode ^= (hashCode >>> 20) ^ (hashCode >>> 12);
      return hashCode ^ (hashCode >>> 7) ^ (hashCode >>> 4);
    }
  }
  private static class Ngram {

    private List<String> toks;
    private int hash = 0;

    public Ngram(List<String> toks) {
      this.toks = toks;
    }

    public int hashCode() {
      if (hash == 0) {
        for(String tok : toks) {
          hash ^= HashUtil.smear(tok.hashCode());
        }
      }
      return hash;
    }

    public boolean equals(Object obj) {
      if (obj instanceof Ngram) {
        // TODO: Slow
        Ngram other = (Ngram) obj;
        return toks.equals(other.toks);
      } else {
        throw new RuntimeException("Comparing n-gram to non-n-gram");
      }
    }

    public String toString() {
      return toks.toString();
    }
  }

  // TODO: Support BLEU other than BLEU=4
  private static final int N = 4;

  public static final String VERSION = "0.1.1";

  public int verbosity = 0;

  public JBLEU() {
  }

  public JBLEU(int verbosity) {
    this.verbosity = verbosity;
  }

  /**
   * Picks the reference in @refs that is closest to @hyp in length and breaks ties by choosing the one with the smallest brevity penalty.
   * @param hyp
   * @param refs
   * @param verbosity
   * @return
   */
  public static int pickReference(List<String> hyp, List<List<String>> refs, int verbosity) {
    int hypLen = hyp.size();
    int selectedRefLen = Integer.MAX_VALUE;
    int selectedRef = -1;
    // TODO: "Closest" or "least harsh"?
    // TODO: Use "least harsh" to break ties betweeen references of equal closeness...
    int curDist = Integer.MAX_VALUE;
    int i = 0;
    for(List<String> ref : refs) {
      // for now, always use closest ref
      int myDist = Math.abs(hypLen - ref.size());
      if (myDist < curDist) {
        selectedRefLen = ref.size();
        selectedRef = i;
        curDist = myDist;
      } else if (myDist == curDist) {
        // break ties based on having a more optimistic brevity penalty (shorter reference)
        if (ref.size() < selectedRefLen) {
          selectedRefLen = ref.size();
          selectedRef = i;
          curDist = myDist;
          if (verbosity >= 2) {
            System.err.println(String.format("jBLEU: Picking more optimistic reference for brevity penalty: hyp_len = %d; ref_len = %d; distance = %d", hypLen, ref.size(), myDist));
          }
        }
      }
      i++;
    }
    return selectedRef;
  }

  public void stats(List<String> hyp, List<List<String>> refs, int[] result) {
    assert result.length == 9;
    assert refs.size() > 0;

    // 1) choose reference length
    int selectedRef = pickReference(hyp, refs, verbosity);
    int selectedRefLen = refs.get(selectedRef).size();

    // TODO: Integer-ify everything inside Ngram? Or is there too much
    // overhead there?

    // 2) determine the bag of n-grams we can score against
    // build a simple tries
    Multiset<Ngram> clippedRefNgrams = HashMultiset.create();
    for(List<String> ref : refs) {
      Multiset<Ngram> refNgrams = HashMultiset.create();
      for(int order = 1; order <= N; order++) {
        for(int i = 0; i <= ref.size() - order; i++) {
          List<String> toks = ref.subList(i, i + order);
          Ngram ngram = new Ngram(toks);
          refNgrams.add(ngram);
        }
      }
      // clip n-grams by taking the maximum number of counts for any given reference
      for(Ngram ngram : refNgrams) {
        int clippedCount = Math.max(refNgrams.count(ngram), clippedRefNgrams.count(ngram));
        clippedRefNgrams.setCount(ngram, clippedCount);
      }
    }

    // 3) now match n-grams
    int[] attempts = new int[N];
    int[] matches = new int[N];
    for(int order = 1; order <= N; order++) {
      for(int i = 0; i <= hyp.size() - order; i++) {
        List<String> toks = hyp.subList(i, i + order);
        Ngram ngram = new Ngram(toks);
        boolean found = clippedRefNgrams.remove(ngram);
        ++attempts[order - 1];
        if (found) {
          ++matches[order - 1];
        }
      }
    }

    // 4) assign sufficient stats
    System.arraycopy(attempts, 0, result, 0, N);
    System.arraycopy(matches, 0, result, N, N);
    result[N*2] = selectedRefLen;
  }

  private static double getAttemptedNgrams(int[] suffStats, int j) {
    return suffStats[j];
  }

  private static double getMatchingNgrams(int[] suffStats, int j) {
    return suffStats[j + N];
  }

  private static double getRefWords(int[] suffStats) {
    return suffStats[N * 2];
  }

  public double score(int[] suffStats) {
    return score(suffStats, null);
  }

  // ###############################################################################################################################
  // # Default method used to compute the BLEU score, using smoothing.
  // # Note that the method used can be overridden using the '--no-smoothing'
  // command-line argument
  // # The smoothing is computed by taking 1 / ( 2^k ), instead of 0, for each
  // precision score whose matching n-gram count is null
  // # k is 1 for the first 'n' value for which the n-gram match count is null
  // # For example, if the text contains:
  // # - one 2-gram match
  // # - and (consequently) two 1-gram matches
  // # the n-gram count for each individual precision score would be:
  // # - n=1 => prec_count = 2 (two unigrams)
  // # - n=2 => prec_count = 1 (one bigram)
  // # - n=3 => prec_count = 1/2 (no trigram, taking 'smoothed' value of 1 / (
  // 2^k ), with k=1)
  // # - n=4 => prec_count = 1/4 (no fourgram, taking 'smoothed' value of 1 /
  // ( 2^k ), with k=2)
  // ###############################################################################################################################
  // segment-level bleu smoothing is done by default and is similar to that of
  // bleu-1.04.pl (IBM)
  //
  // if allResults is non-null it must be of length N+1 and it
  // will contain bleu1, bleu2, bleu3, bleu4, brevity penalty
  public double score(int[] suffStats, double[] allResults) {
    Preconditions.checkArgument(suffStats.length == N * 2 + 1, "BLEU sufficient stats must be of length N*2+1");

    final double brevityPenalty;
    double refWords = getRefWords(suffStats);
    double hypWords = getAttemptedNgrams(suffStats, 0);
    if (hypWords < refWords) {
      brevityPenalty = Math.exp(1.0 - refWords / hypWords);
    } else {
      brevityPenalty = 1.0;
    }
    assert brevityPenalty >= 0.0;
    assert brevityPenalty <= 1.0;
    if (verbosity >= 1) {
      System.err.println(String.format("jBLEU: Brevity penalty = %.6f (ref_words = %.0f, hyp_words = %.0f)", brevityPenalty, refWords, hypWords));
    }

    if (allResults != null) {
      assert allResults.length == N + 1;
      allResults[N] = brevityPenalty;
    }

    double score = 0.0;
    double smooth = 1.0;

    for(int j = 0; j < N; j++) {
      double attemptedNgramsJ = getAttemptedNgrams(suffStats, j);
      double matchingNgramsJ = getMatchingNgrams(suffStats, j);
      final double iscore;
      if (attemptedNgramsJ == 0) {
        iscore = 0.0;
        if (verbosity >= 1) System.err.println(String.format("jBLEU: %d-grams: raw 0/0 = 0 %%", j+1));
      } else if (matchingNgramsJ == 0) {
        smooth *= 2;
        double smoothedPrecision = 1.0 / (smooth * attemptedNgramsJ);
        iscore = Math.log(smoothedPrecision);
        if (verbosity >= 1) System.err.println(String.format("jBLEU: %d-grams: %.0f/%.0f = %.2f %% (smoothed) :: raw = %.2f %%", j+1, matchingNgramsJ, attemptedNgramsJ, smoothedPrecision * 100, matchingNgramsJ / attemptedNgramsJ * 100));
      } else {
        double precisionAtJ = matchingNgramsJ / attemptedNgramsJ;
        iscore = Math.log(precisionAtJ);
        if (verbosity >= 1) System.err.println(String.format("jBLEU: %d-grams: %.0f/%.0f = %.2f %% (unsmoothed)", j+1, matchingNgramsJ, attemptedNgramsJ, precisionAtJ * 100));
      }
      // TODO: Allow non-uniform weights instead of just the "baseline"
      // 1/4 from Papenini
      double ngramOrderWeight = 0.25;
      score += iscore * ngramOrderWeight;

      if (allResults != null) {
        assert allResults.length == N + 1;
        allResults[j] = brevityPenalty * Math.exp(score);
      }

      // assert Math.exp(iscore * ngramOrderWeight) <= 1.0 :
      // String.format("ERROR for order %d-grams iscore: %f -> %f :: %s",
      // j+1, iscore, Math.exp(iscore * ngramOrderWeight),
      // Arrays.toString(suffStats));
      // assert Math.exp(score * ngramOrderWeight) <= 1.0;
    }

    double totalScore = brevityPenalty * Math.exp(score);

    if (totalScore > 1.0) {
      System.err.println("BLEU: Thresholding out of range score: " + totalScore + "; stats: "
          + Arrays.toString(suffStats));
      totalScore = 1.0;
    } else if (totalScore < 0.0) {
      System.err.println("BLEU: Thresholding out of range score: " + totalScore);
      totalScore = 0.0;
    }

    return totalScore;
  }

  public static int getSuffStatCount() {
    // attempted 1-4 gram counts
    // matching 1-4 gram counts
    // length of selected reference for brevity penalty
    return N * 2 + 1;
  }

  @ArgumentParser.Option(name="input", required=true, gloss="input file: <candidate>(\\t<references>)+")
  public static String filename;

  public List<Pair<List<String>, List<List<String>>>> parseInput(String filename) throws FileNotFoundException {
    RecordIterator rit = new RecordIterator(filename, "\t");
    List<Pair<List<String>, List<List<String>>>> ret = new ArrayList<>();
    while(rit.hasNext()) {
      List<String> parts = rit.next();
      List<List<String>> sentences = parts.stream().map(Sentence::new).map(Sentence::words).collect(Collectors.toList());
      ret.add(Pair.makePair(sentences.get(0), sentences.subList(1, sentences.size())));
    }
    return ret;
  }

  public void run() {
    int[] result = new int[JBLEU.getSuffStatCount()];

    double avg = 0.; double count = 0.;
    try {
      for(Pair<List<String>, List<List<String>>> pair : parseInput(filename)) {
        List<String> hyp = pair.first();
        List<List<String>> refs = pair.second();
        log(hyp);
        log(refs);
        stats(hyp, refs, result);
        double score = score(result);
        avg += (score - avg)/(count + 1);
        count += 1;
        log(score);
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    System.out.println(String.format("BLEU:\t%.4f", avg));
  }

  public static void main(String[] args) {
    ArgumentParser.fillOptions(JBLEU.class, args);
    new JBLEU().run();
  }
}
