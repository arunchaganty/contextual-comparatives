package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.simple.Sentence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Test for the numeric mention patterns
 */
public class NumericMentionPatternTest {

  NumericMentionAnnotator ann;
  @Before
  public void init() {
    ann = new NumericMentionAnnotator();
  }

  public int countMatches(TokenSequencePattern pattern, String sent) {
    return new Sentence(sent).find(pattern, m -> 0).size();
  }

  public boolean testListEquality(List<NumericMention> n1, List<NumericMention> n2) {
    assert n1.size() == n2.size();
    for(int i = 0; i < n1.size(); i++)
      if (!n1.get(i).valueEquals(n2.get(i)))
        return false;
    return true;
  }

  @Test
  public void testMoneyPattern() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is US$ 100."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "USD")), ann.annotate(new Sentence("This burrito is USD 100."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is $1,000."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(1000, "$")), ann.annotate(new Sentence("This burrito is $1,000."))));
    Assert.assertEquals(0, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is a 100."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is USD 100 thousand."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100*1e3, "USD")), ann.annotate(new Sentence("This burrito is USD 100 thousand."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is USD 100 thousand million billion."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100*1e3*1e6*1e9, "USD")), ann.annotate(new Sentence("This burrito is USD 100 thousand million billion."))));
    Assert.assertEquals(2, countMatches(NumericMentionAnnotator.MONEY_PATTERN, "This burrito is USD 100 thousand million billion, but that burrito is only $0.99."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100*1e3*1e6*1e9, "USD"), NumericMention.of(0.99,"$")), ann.annotate(new Sentence("This burrito is USD 100 thousand million billion, but that burrito is only $0.99."))));
  }

  @Test
  public void testMoneyPatternTrouble() {
    String sent = "Sen. Tom Coburn, R-Okla., released a plan late Wednesday for $126 billion in savings, including a $100 million cut in congressional budgets, a freeze on federal salaries and a $10 billion reduction in government travel.";
    Assert.assertEquals(3, countMatches(NumericMentionAnnotator.MONEY_PATTERN, sent));
    log(ann.annotate(new Sentence(sent)));
    Assert.assertTrue(testListEquality(Arrays.asList(
        NumericMention.of(126e9, "$"),
        NumericMention.of(100e6, "$"),
        NumericMention.of(10e9, "$")),
        ann.annotate(new Sentence(sent))));
  }

  @Test
  public void testWeight() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 kilograms."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "kilograms")), ann.annotate(new Sentence("This burrito is a 100 kilograms."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 thousand kilograms."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100 * 1e3, "kilograms")), ann.annotate(new Sentence("This burrito is a 100 thousand kilograms."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 kg."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "kg")), ann.annotate(new Sentence("This burrito is a 100 kg."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 pounds."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "pounds")), ann.annotate(new Sentence("This burrito is a 100 pounds."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 grams."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "grams")), ann.annotate(new Sentence("This burrito is a 100 grams."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.WEIGHT_PATTERN, "This burrito is a 100 billion tons."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100 * 1e9, "tons")), ann.annotate(new Sentence("This burrito is a 100 billion tons."))));
  }

  @Test
  public void testLength() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 1 inch."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(1, "inch")), ann.annotate(new Sentence("This burrito is a 1 inch."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 inches."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "inches")), ann.annotate(new Sentence("This burrito is a 10 inches."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "in")), ann.annotate(new Sentence("This burrito is a 10 in."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 billion in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10*1e9, "in")), ann.annotate(new Sentence("This burrito is a 10 billion in."))));


//    env.bind("$LENGTH", "/(((millimeter)|(centimeter)|(meter)|(kilometer)|(inch)|(inche)|(foot)|(feet)|(yard)|(mile))s?)|((mm)|(cm)|(m)|(km)|(in)|(ft)|(yd)|(mi)).?s?/");
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 millimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 centimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 meter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 kilometer."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 inch."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 inches."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 foot."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 feet."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 yards."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 miles."));
//    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10mm.")); // KNOWN BUG
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 cm."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 m."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 km."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 in."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 ft."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 yd."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.LENGTH_PATTERN, "This burrito is a 10 mi."));
  }

  @Test
  public void testArea() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 1 square inch."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(1, "square inch")), ann.annotate(new Sentence("This burrito is a 1 square inch."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq inches."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "sq inches")), ann.annotate(new Sentence("This burrito is a 10 sq inches."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "sq. in")), ann.annotate(new Sentence("This burrito is a 10 sq. in."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 billion sq in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10*1e9, "sq. in")), ann.annotate(new Sentence("This burrito is a 10 billion sq. in."))));


//    env.bind("$AREA", "/(((millimeter)|(centimeter)|(meter)|(kilometer)|(inch)|(inche)|(foot)|(feet)|(yard)|(mile))s?)|((mm)|(cm)|(m)|(km)|(in)|(ft)|(yd)|(mi)).?s?/");
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. millimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. centimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. meter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. kilometer."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. inch."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. inches."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. foot."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. feet."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. yards."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. miles."));
//    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10mm.")); // KNOWN BUG
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. cm."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. m."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. km."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. in."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. ft."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. yd."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.AREA_PATTERN, "This burrito is a 10 sq. mi."));
  }

  @Test
  public void testVolume() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 1 cubic inch."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(1, "cubic inch")), ann.annotate(new Sentence("This burrito is a 1 cubic inch."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu inches."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "cu inches")), ann.annotate(new Sentence("This burrito is a 10 cu inches."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10, "cu. in")), ann.annotate(new Sentence("This burrito is a 10 cu. in."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 billion cu in."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(10*1e9, "cu. in")), ann.annotate(new Sentence("This burrito is a 10 billion cu. in."))));


//    env.bind("$VOLUME", "/(((millimeter)|(centimeter)|(meter)|(kilometer)|(inch)|(inche)|(foot)|(feet)|(yard)|(mile))s?)|((mm)|(cm)|(m)|(km)|(in)|(ft)|(yd)|(mi)).?s?/");
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. millimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. centimeter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. meter."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. kilometer."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. inch."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. inches."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. foot."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. feet."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. yards."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. miles."));
//    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10mm.")); // KNOWN BUG
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. cm."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. m."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. km."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. in."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. ft."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. yd."));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.VOLUME_PATTERN, "This burrito is a 10 cu. mi."));
  }

  @Test
  public void testEntity() {
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 people."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "people")), ann.annotate(new Sentence("This burrito is made of 100 people."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 thousand people."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100 * 1e3, "people")), ann.annotate(new Sentence("This burrito is made of 100 thousand people."))));

    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 guns."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "guns")), ann.annotate(new Sentence("This burrito is made of 100 guns."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 thousand guns."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100 * 1e3, "guns")), ann.annotate(new Sentence("This burrito is made of 100 thousand guns."))));

    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 cars."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100, "cars")), ann.annotate(new Sentence("This burrito is made of 100 cars."))));
    Assert.assertEquals(1, countMatches(NumericMentionAnnotator.ENTITY_PATTERN, "This burrito is made of 100 thousand cars."));
    Assert.assertTrue(testListEquality(Arrays.asList(NumericMention.of(100 * 1e3, "cars")), ann.annotate(new Sentence("This burrito is made of 100 thousand cars."))));
  }



}
