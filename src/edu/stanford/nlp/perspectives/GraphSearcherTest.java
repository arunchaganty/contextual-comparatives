package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.Util;
import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.logging.Redwood;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static edu.stanford.nlp.perspectives.NumericTupleTest.*;

/**
 * Test the graph search.
 */
public class GraphSearcherTest {
  @Test
  public void testEmptyGraph() {
    TupleDatabase db = TupleDatabase.compile(Collections.emptyList());
    db.addVertex(Unit.of("USD"));
    GraphSearcher searcher = new GraphSearcher(db);

    List<Expr> exprs = searcher.generate(NumericMention.of(100, "USD"));
    Assert.assertEquals(0, exprs.size());
  }


  @Test
  public void testSingletonGraph() {
    GraphSearcher searcher = new GraphSearcher(TupleDatabase.compile(Collections.singletonList(nasa)));

    List<Expr> exprs = searcher.generate(NumericMention.of(100, "USD"));
    Assert.assertEquals(1, exprs.size());
  }

  @Test
  public void testUSDGraph() {
    GraphSearcher searcher = new GraphSearcher(TupleDatabaseTest.budgetDb);

    List<Expr> exprs = searcher.generate(NumericMention.of(100, "USD"));
    Assert.assertEquals(2, exprs.size());
  }

  @Test
  public void testFoodPerYear() {
    GraphSearcher searcher = new GraphSearcher(TupleDatabaseTest.fullDb);

    List<Expr> exprs = searcher.generate(NumericMention.of(100, "USD per year"));
    Assert.assertEquals(2, exprs.size());
  }

  @Test
  public void testUSD() {
    GraphSearcher searcher = new GraphSearcher(TupleDatabaseTest.fullDb);

    List<Expr> exprs = searcher.generate(NumericMention.of(100, "USD"));
    exprs.stream().map(Expr::evaluate).forEach(Redwood.Util::log);
    Assert.assertEquals(5, exprs.size());
  }

  static final String EMBEDDINGS_DATA = "/home/chaganty/Research/resources/wiki.bolt.giga5.f100.unk.neg5.50.txt";
  TupleDatabase tdb;
  UnitDatabase udb;
  @Before
  public void loadTDB() {
    udb = UnitDatabase.readFromTSV(TupleDatabaseTest.UNITS_FILE);
    tdb = TupleDatabase.compile(Util.readNumericData(TupleDatabaseTest.DB_FILE), udb);
  }

  @Test
  public void testGallon() {
    GraphSearcher searcher = new GraphSearcher(tdb);

    List<Expr> exprs = searcher.generate(udb.standardizeUnits(NumericMention.of(1, "gallon")));
    exprs.stream().map(Expr::evaluate).forEach(Redwood.Util::log);
    Assert.assertEquals(377, exprs.size());
  }

  @Test
  public void testReorderGallon() {
    GraphSearcher searcher = new GraphSearcher(tdb);

    NumericMention mention = NumericMention.of(1, "gallon", "The milk was 1 gallon.");
    List<Expr> exprs = searcher.generate(udb.standardizeUnits(mention));
    searcher.reorder(mention, exprs).stream().map(p -> Pair.makePair(p.second, p.first.evaluate())).forEach(Redwood.Util::log);
    Assert.assertEquals(377, exprs.size());
  }


}
