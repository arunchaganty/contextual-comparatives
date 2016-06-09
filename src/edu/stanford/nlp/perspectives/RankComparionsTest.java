package edu.stanford.nlp.perspectives;

import org.junit.Test;

import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Tests for generating ranked comparisons.
 */
public class RankComparionsTest {

  private static final String WORD_EMBEDDINGS_FILE = "/home/chaganty/Research/resources/wiki.bolt.giga5.f100.unk.neg5.50.txt";
  private final TupleDatabase data = TupleDatabaseTest.fullDb;
  private final Util.WordEmbedding embeddings = Util.WordEmbedding.loadFromFile(WORD_EMBEDDINGS_FILE);
  private final GraphSearcher searcher = new GraphSearcher(data, embeddings);

  @Test
  public void testNasaBudget() {
    NumericMention mention = NumericMentionTest.nasaBudget;
    log(mention.sentence.get());
    for (int i =0; i < 10; i++)
      log(searcher.generate(mention).get(0).evaluate().comparisonNP(mention.value, mention.unit));
  }


}
