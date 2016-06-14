package edu.stanford.nlp.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static edu.stanford.nlp.util.logging.Redwood.log;

/**
 * Test JBLEU class
 */
public class JBLEUTest {

  @Test
  public void testScore() {
    JBLEU bleu = new JBLEU();

    int[] result = new int[JBLEU.getSuffStatCount()];

    List<List<String>> refs = new ArrayList<>();
    refs.add(Arrays.asList("I always do .".split("\\s+")));
    refs.add(Arrays.asList("I always do .".split("\\s+")));
    refs.add(Arrays.asList("I always invariably do .".split("\\s+")));
    refs.add(Arrays.asList("I always perpetually do .".split("\\s+")));

    {
      List<String> hyp = Arrays.asList("I always invariably do .".split("\\s+"));
      log(hyp);
      log(refs);
      bleu.stats(hyp, refs, result);
      double score = bleu.score(result);

      System.err.println(Arrays.toString(result));
      System.err.println(score);

      Assert.assertArrayEquals(new int[]{5, 4, 3, 2, 5, 4, 3, 2, 5}, result);
      Assert.assertEquals(1.0, score, 1e-2);
    }
    {
      List<String> hyp = Arrays.asList("I always invariably perpetually do .".split("\\s+"));
      log(hyp);
      log(refs);
      bleu.stats(hyp, refs, result);
      double score = bleu.score(result);

      System.err.println(Arrays.toString(result));
      System.err.println(score);

      Assert.assertArrayEquals(new int[]{6, 5, 4, 3, 6, 4, 2, 0, 5}, result);
      Assert.assertEquals(0.508, score, 1e-2);
    }
  }
}
