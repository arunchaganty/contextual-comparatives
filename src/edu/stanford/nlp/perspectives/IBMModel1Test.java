package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Created by chaganty on 2/22/16.
 */
public class IBMModel1Test {

  static final List<Pair<String, String>> biCorpus = new ArrayList<Pair<String, String>>() {{
    add(Pair.makePair("the house", "la casa"));
    add(Pair.makePair("green house", "casa verde"));
  }};

  static final Map<String, Counter<String>> translationMap = new HashMap<String, Counter<String>>() {{
    Counter<String> green = new ClassicCounter<>();
    green.incrementCount("casa", 0.05);
    green.incrementCount("verde", 0.95);
    green.incrementCount("la", 0.00);
    put("green", green);

    Counter<String> house = new ClassicCounter<>();
    house.incrementCount("casa", 0.90);
    house.incrementCount("verde", 0.05);
    house.incrementCount("la", 0.05);
    put("house", house);

    Counter<String> the = new ClassicCounter<>();
    the.incrementCount("casa", 0.05);
    the.incrementCount("verde", 0.0);
    the.incrementCount("la", 0.95);
    put("the", the);
  }};

  static final Map<Integer, Counter<Integer>> lengthMap = new HashMap<Integer, Counter<Integer>>() {{
    for(int i = 1; i < 10; i++) {
      Counter<Integer> cnt = new ClassicCounter<>();
      cnt.incrementCount(i);
      put(i, cnt);
    }
  }};

  @Test
  public void testGeneration() {
    IBMModel1 model = new IBMModel1(lengthMap, translationMap);
    for(int i = 0; i < 100; i++) {
      log(model.generate("the house"));
      log(model.generate("green house"));
    }
  }

  @Test
  public void testAlignment() {
    IBMModel1 model = new IBMModel1(lengthMap, translationMap);
    Assert.assertArrayEquals(new int[]{0,1},model.bestAlignment("the house".split(" "), "la casa".split(" ")));
    Assert.assertArrayEquals(new int[]{1,0},model.bestAlignment("green house".split(" "), "casa verde".split(" ")));
  }

  @Test
  public void testIntialize() {
    IBMModel1 model = IBMModel1.fromBiCorpus(biCorpus);
    log(model.translationMap);
  }

  @Test
  public void testTrain() {
    IBMModel1 model = IBMModel1.fromBiCorpus(biCorpus);
    for(int i = 0; i < 10; i++)
      model.doEMStep(biCorpus);
    log(model.translationMap);
    Assert.assertTrue("'the' does not tranlsate to 'la'", model.translationMap.get("the").getCount("la") > 0.9);
    Assert.assertTrue("'green' does not tranlsate to 'verde'", model.translationMap.get("green").getCount("verde") > 0.9);
    Assert.assertTrue("'house' does not tranlsate to 'casas'", model.translationMap.get("house").getCount("casa") > 0.9);
  }
}
