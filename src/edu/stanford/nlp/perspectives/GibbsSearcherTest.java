package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import org.junit.Test;

import java.util.*;

import static edu.stanford.nlp.util.logging.Redwood.log;

/**
 * Created by chaganty on 2/3/16.
 */
public class GibbsSearcherTest {

  @Test
  public void searcherTest() {
    GibbsSearcher gibbsSearcher = new GibbsSearcher(TupleDatabaseTest.fullDb);
    log(gibbsSearcher.generate(Optional.empty(), new ExprBuilder()));
  }

  @Test
  // The search should find something
  public void searcherTestForUSD() {
    GibbsSearcher gibbsSearcher = new GibbsSearcher(TupleDatabaseTest.budgetDb);
    log(gibbsSearcher.generate(Optional.empty(), new ExprBuilder(Unit.of("US$"))));
  }

  @Test
  public void searcherTestForUSDyr() {
    GibbsSearcher gibbsSearcher = new GibbsSearcher(TupleDatabaseTest.fullDb);
    log(gibbsSearcher.generate(Optional.empty(), new ExprBuilder(Unit.of("US$ per year"))));
  }

  @Test
  public void searcherTestForUSDmo() {
    List<UnitTuple> unitTuples = new ArrayList<>();
    unitTuples.add(UnitTuple.of("year", 12, "month"));
    unitTuples.add(UnitTuple.of("year", 12, "month").invert());
    unitTuples.add(UnitTuple.of("year", 12, "month").symmetric());
    unitTuples.add(UnitTuple.of("year", 12, "month").symmetric().invert());

    final NumericTuple foodCostMo  =
        new NumericTuple("food cost", "is", 300, "US$ per month");
    final NumericTuple foodCostYr  =
        new NumericTuple("food cost", "is", 300*12, "US$ per year");

    {
      GibbsSearcher gibbsSearcher = new GibbsSearcher(TupleDatabase.compile(Collections.singletonList(foodCostYr)), Util.WordEmbedding.empty(), unitTuples);
      log(gibbsSearcher.generate(Optional.empty(), new ExprBuilder(Unit.of("US$ per month"))));
    }

    {
      GibbsSearcher gibbsSearcher = new GibbsSearcher(TupleDatabase.compile(Collections.singletonList(foodCostMo)), Util.WordEmbedding.empty(), unitTuples);
      log(gibbsSearcher.generate(Optional.empty(), new ExprBuilder(Unit.of("US$ per year"))));
    }
  }

}
