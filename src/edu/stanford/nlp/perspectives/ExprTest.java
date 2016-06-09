package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.util.Pair;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by chaganty on 3/3/16.
 */
public class ExprTest {

  @Test
  public void testExprOf() {
    Expr expr = Expr.of("19990000.000000 person (number of employees in the services industry) * 1.000000 day (a day) * 71000.000000 USD per year per person (cost of an employee)");
    Assert.assertEquals(NumericTuple.of("number of employees in the services industry", "is", 19990000.000000, "person"), expr.leftArg.get().leftArg.get().tupleArg.get());
    Assert.assertEquals(NumericTuple.of("a day", "is", 1.000000, "day"), expr.leftArg.get().rightArg.get().tupleArg.get());
    Assert.assertEquals(NumericTuple.of("cost of an employee", "is", 71000.000000, "USD per year per person"), expr.rightArg.get().tupleArg.get());
  }

  @Test
  public void testExprOfMultiplier() {
    Pair<Double, Expr> pair = Expr.ofWithMultiplier("0.257171 * 19990000.000000 person (number of employees in the services industry) * 1.000000 day (a day) * 71000.000000 USD per year per person (cost of an employee)");
    Double multiplier = pair.first;
    Expr expr = pair.second;
    Assert.assertEquals(0.257171, multiplier, 1e-4);
    Assert.assertEquals(NumericTuple.of("number of employees in the services industry", "is", 19990000.000000, "person"), expr.leftArg.get().leftArg.get().tupleArg.get());
    Assert.assertEquals(NumericTuple.of("a day", "is", 1.000000, "day"), expr.leftArg.get().rightArg.get().tupleArg.get());
    Assert.assertEquals(NumericTuple.of("cost of an employee", "is", 71000.000000, "USD per year per person"), expr.rightArg.get().tupleArg.get());
  }
}
