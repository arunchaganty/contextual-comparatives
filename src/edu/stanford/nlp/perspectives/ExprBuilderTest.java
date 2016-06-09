package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.Util;
import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

import static edu.stanford.nlp.perspectives.NumericTupleTest.*;

/**
 * Tests for an expression builder.
 */
public class ExprBuilderTest {
  @Test
  public void testTupleWithoutUnit() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.empty());
    Util.Either<ExprBuilder, Expr> ret = builder.tuple(nasa);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(nasa));
  }

  @Test
  public void testTupleUnit() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$")));
    Util.Either<ExprBuilder, Expr> ret = builder.tuple(nasa);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(nasa));
  }

  @Test(expected=AssertionError.class)
  public void testTupleWrongUnit() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("mi")));
    Util.Either<ExprBuilder, Expr> ret = builder.tuple(nasa);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(nasa));
  }

  @Test
  public void testAddition() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$")));
    Util.Either<ExprBuilder, Expr> ret;

    builder = builder.op(Expr.Operation.ADD);

    ret = builder.tuple(nasa);
    Assert.assertTrue(ret.left.isPresent());
    builder = ret.left.get();

    ret = builder.tuple(militaryBudget);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(nasa.add(militaryBudget)));
  }

  @Test(expected = AssertionError.class)
  public void testAdditionWrongType() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$")));
    Util.Either<ExprBuilder, Expr> ret;

    builder = builder.op(Expr.Operation.ADD);

    ret = builder.tuple(nasa);
    Assert.assertTrue(ret.left.isPresent());
    builder = ret.left.get();

    ret = builder.tuple(cars_in_India);
    Assert.assertTrue(ret.right.isPresent());
  }

  @Test
  public void testMultiply() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$ per year")));
    Util.Either<ExprBuilder, Expr> ret;

    builder = builder.op(Expr.Operation.MUL);

    ret = builder.tuple(foodCostYr);
    Assert.assertTrue(ret.left.isPresent());
    builder = ret.left.get();

    ret = builder.tuple(LA);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(foodCostYr.mul(LA)));
  }

  @Test(expected = AssertionError.class)
  public void testMultiplyWrongType() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$ per year")));
    Util.Either<ExprBuilder, Expr> ret;

    builder = builder.op(Expr.Operation.MUL);

    ret = builder.tuple(usBudget);
    Assert.assertTrue(ret.left.isPresent());
    builder = ret.left.get();

    ret = builder.tuple(LA);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(foodCostYr.mul(LA)));
  }

  @Test
  public void testUnitChange() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$ per person per year")));
    Util.Either<ExprBuilder, Expr> ret;

    UnitTuple monthsPerYear = UnitTuple.of("month", 12, "year");

    builder = builder.unitChange(monthsPerYear);

    ret = builder.tuple(foodCost);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(foodCost.unitChange(monthsPerYear)));
  }

  @Test
  public void testUnitChangeMultiply() {
    ExprBuilder builder = new ExprBuilder(Optional.empty(), Optional.empty(), Optional.of(NumericTuple.Unit.of("US$ per year")));
    Util.Either<ExprBuilder, Expr> ret;

    UnitTuple monthsPerYear = UnitTuple.of("per month", 12, "per year");

    builder = builder.op(Expr.Operation.MUL);
    builder = builder.unitChange(monthsPerYear);

    ret = builder.tuple(foodCost);
    Assert.assertTrue(ret.left.isPresent());
    builder = ret.left.get();

    ret = builder.tuple(LA);
    Assert.assertTrue(ret.right.isPresent());
    Assert.assertTrue(ret.right.get().evaluate().equals(foodCost.unitChange(monthsPerYear).mul(LA)));
  }


}
