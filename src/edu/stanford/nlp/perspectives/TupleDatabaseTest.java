package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static edu.stanford.nlp.perspectives.NumericTupleTest.*;
import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Created by chaganty on 2/8/16.
 */
public class TupleDatabaseTest {

  public static final TupleDatabase budgetDb =
      TupleDatabase.compile(
          new ArrayList<NumericTuple>(){{
//          add(nasa);
            add(militaryBudget);
            add(usBudget);
          }}
      );

  public static final TupleDatabase foodCostDb =
      TupleDatabase.compile(
          new ArrayList<NumericTuple>(){{
            add(foodCost);
            add(foodCostYr);
            add(LA);
          }}
      );

  public static final TupleDatabase carsDb =
      TupleDatabase.compile(
          new ArrayList<NumericTuple>(){{
//          add(cars_in_US);
            add(cars_in_India);
            add(people_in_US);
            add(LAtoSF);
            add(gasMileage);
            add(worldProd);
          }}
      );

  public static final TupleDatabase fullDb =
      TupleDatabase.compile(
          new ArrayList<NumericTuple>() {{
            add(nasa);
            add(militaryBudget);
            add(usBudget);
            add(foodCost);
            add(foodCostYr);
            add(LA);
            add(cars_in_India);
            add(people_in_US);
            add(LAtoSF);
            add(gasMileage);
            add(worldProd);
            add(aYear);
          }},
          new UnitDatabase() {{
            add(UnitTuple.of("barrel", 20, "gallon"));
            add(UnitTuple.of("year", 365, "day"));
            add(UnitTuple.of("year", 12, "month"));
          }}
      );


  // This is a pseduo-test -- it's more about running something that outputs a graph for debugging.
  @Test
  public void testSmallDb() throws IOException {
    final TupleDatabase db = fullDb;
    IOUtils.writeStringToFile(db.graphToDot(), "small-db.dot", "UTF-8");
  }

  // Big test
  protected static final String DB_FILE = System.getenv("HOME") + "/Research/contextual-comparatives/data/cc_numericdata.tsv";
  protected static final String UNITS_FILE = System.getenv("HOME") + "/Research/contextual-comparatives/data/unit_conversions.tsv";


  // This is a pseduo-test -- it's more about running something that outputs a graph for debugging.
  @Test
  public void testBigDb() throws IOException {
    final TupleDatabase db =
        TupleDatabase.compile(
            Util.readNumericData(DB_FILE),
            UnitDatabase.readFromTSV(UNITS_FILE)
        );
    log("total size: ", db.totalSize());

    IOUtils.writeStringToFile(db.graphToDot(), "big-db.dot", "UTF-8");
  }


}
