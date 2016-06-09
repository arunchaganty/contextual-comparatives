package edu.stanford.nlp.perspectives;

import org.junit.Assert;
import org.junit.Test;

import static edu.stanford.nlp.perspectives.NumericTuple.*;
import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Test behavior of unit conversions
 */
public class UnitTupleTest {
  @Test
  public void testConversionAtUnit() {
    Unit u1 = Unit.of("meter");
    Unit u2 = Unit.of("centimeter");
    UnitTuple ut = UnitTuple.of("meter", 100, "centimeter");
    log(u1.convert(ut));
    Assert.assertEquals(u2, u1.convert(ut));
  }

  @Test
  public void testConversionAtTuple() {
    Unit u1 = Unit.of("meter");
    UnitTuple ut = UnitTuple.of("meter", 100, "centimeter");
    NumericTuple t1 = new NumericTuple("a meter", "is", 1, "meter");
    NumericTuple t2 = new NumericTuple("a meter", "is", 100, "centimeter");
    log(t1.unitChange(ut));
    Assert.assertEquals(t2, t1.unitChange(ut));
  }

  @Test
  public void testConversionAtExpr() {
    Unit u1 = Unit.of("meter");
    Unit u2 = Unit.of("centimeter");
    UnitTuple ut = UnitTuple.of("meter", 100, "centimeter");

    NumericTuple t1 = new NumericTuple("a meter", "is", 1, "meter");
    NumericTuple t2 = new NumericTuple("a meter", "is", 100, "centimeter");

    Expr e1 = Expr.tuple(t1);
    Expr e2 = Expr.unitChange(e1, ut);
    log(e2.evaluate());
    Assert.assertEquals(t2, e2.evaluate());
  }
}
