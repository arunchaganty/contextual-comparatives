package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.perspectives.Util.Either;

import java.util.Optional;

import static edu.stanford.nlp.perspectives.Expr.*;

/**
 * Represents a (full) expression tree.
 */
public class ExprBuilder {
  public final Optional<ExprBuilder> parent; // To traverse the expression tree.

  public int depth;

  public Optional<Operation> op;
  // TODO: clear builders after exprs have been built.
  public Optional<ExprBuilder> leftArgBuilder;
  public Optional<ExprBuilder> rightArgBuilder;

  public Optional<Expr> leftArg;
  public Optional<Expr> rightArg;
  public Optional<NumericTuple> tupleArg;
  public Optional<UnitTuple> unitArg;

  public Optional<Double> expectedValue;
  public Optional<Unit> expectedUnit;

  protected ExprBuilder(Optional<ExprBuilder> parent, Optional<Double> value, Optional<Unit> unit) {
    this.parent = parent;
    this.depth = parent.map(p -> p.depth+1).orElse(0);
    this.op = Optional.empty();
    this.leftArgBuilder = Optional.empty();
    this.rightArgBuilder = Optional.empty();

    this.leftArg = Optional.empty();
    this.rightArg = Optional.empty();
    this.tupleArg = Optional.empty();
    this.unitArg = Optional.empty();
    this.expectedValue = value;
    this.expectedUnit = unit;
  }
  public ExprBuilder(ExprBuilder copy) {
    this.parent = copy.parent.map(ExprBuilder::new); // :-S recursive copy.
    this.depth = copy.depth;
    this.op = copy.op;
    this.leftArgBuilder = copy.leftArgBuilder;
    this.rightArgBuilder = copy.rightArgBuilder;

    this.leftArg = copy.leftArg;
    this.rightArg = copy.rightArg;
    this.tupleArg = copy.tupleArg;
    this.unitArg = copy.unitArg;
    this.expectedValue = copy.expectedValue;
    this.expectedUnit = copy.expectedUnit;
  }
  public ExprBuilder(Double value, Unit unit) {
    this(Optional.empty(), Optional.of(value), Optional.of(unit));
  }
  public ExprBuilder(Unit unit) {
    this(Optional.empty(), Optional.empty(), Optional.of(unit));
  }
  public ExprBuilder() {
    this(Optional.empty(), Optional.empty(), Optional.empty());
  }

  public ExprBuilder parentest() {
    if (parent.isPresent()) return parent.get().parentest();
    else return this;
  }


  @Override
  public String toString() {
    return op.map(o -> {
      switch (o) {
        case TUPLE:
          return tupleArg.get().toString();
        case UNIT:
          return "(" + leftArgBuilder.map(ExprBuilder::toString).orElse("?") + "): "  + unitArg.get() + " -> " + expectedUnit.map(Unit::toString).orElse("?");
        default:
          return "(" + leftArgBuilder.map(ExprBuilder::toString).orElse("?") + " " + o.toString() + " " + rightArgBuilder.map(ExprBuilder::toString).orElse("?") + "): "  + expectedUnit.map(Unit::toString).orElse("?");
      }
    }).orElse("?:" + expectedUnit.map(Unit::toString).orElse("?"));
  }

  /**
   * Updates unit if not already set. If set, assert the units are the same.
   * @param unit - new unit value.
   * @return true if the unit was updated and false if it was already set.
   */
  public boolean updateUnit(Unit unit) {
    if (expectedUnit.isPresent()) {
      assert expectedUnit.get().equals(unit);
      return false;
    } else {
      expectedUnit = Optional.of(unit);
      return false;
    }
  }

  /**
   * Updates value if not already set.
   * @param value - new value.
   * @return true if the unit was updated and false if it was already set.
   */
  public boolean updateValue(double value) {
    expectedValue = Optional.of(value);
    return true;
  }

  /**
   * Update children with the right units based on the operation.
   */
  protected void updateChildrenAfterLeftDone() {
    assert op.isPresent() && !op.get().isTuple();

    assert leftArg.isPresent();
    ExprBuilder rightArgBuilder_ = rightArgBuilder.get();

    Double lvalue = leftArg.get().value;
    Unit lunit = leftArg.get().unit;
    switch(op.get()) {
      case ADD: // make sure units are equal.
        // Update current node
        updateUnit(lunit);

        // Update sibling
        rightArgBuilder_.updateUnit(lunit);
        expectedValue.ifPresent(v -> rightArgBuilder_.updateValue(v - lvalue));
        break;
      case MUL: // make sure units are equal.
        // Note: the root's unit was either set, or will be updated when right arg builder returns.
        // Update sibling
        expectedUnit.ifPresent(u -> rightArgBuilder_.updateUnit(u.div(lunit)));
        expectedValue.ifPresent(v -> rightArgBuilder_.updateValue(v / lvalue));
        break;
      case DIV: // make sure units are equal.
        // Note: the root's unit was either set, or will be updated when right arg builder returns.
        // Update sibling
        expectedUnit.ifPresent(u -> rightArgBuilder_.updateUnit(lunit.div(u)));
        expectedValue.ifPresent(v -> rightArgBuilder_.updateValue(v * lvalue));
        break;
      default:
        throw new IllegalArgumentException("invalid op " + op.get().toString());
    }
  }

  /**
   * Update children with the right units based on the operation.
   * TODO: per se this is not required. delete?
   */
  protected void updateChildrenAfterRightDone() {
    assert op.isPresent() && op.get().isOp();
    assert leftArg.isPresent();
    assert rightArg.isPresent();
    Expr leftArg_ = leftArg.get();
    Expr rightArg_ = rightArg.get();

    switch(op.get()) {
      case ADD: // make sure units are equal.
        // Make sure that the current type matches childs.
        assert expectedUnit.isPresent();
        assert expectedUnit.get().equals(rightArg_.unit);
        updateValue(leftArg_.value + rightArg_.value);
        break;
      case MUL: // make sure units are equal.
        // If the current type isn't set, update it.
        updateUnit(leftArg_.unit.mul(rightArg_.unit));
        updateValue(leftArg_.value * rightArg_.value);
        break;
      case DIV: // make sure units are equal.
        // If the current type isn't set, update it.
        updateUnit(leftArg_.unit.div(rightArg_.unit));
        updateValue(leftArg_.value / rightArg_.value);
        break;
      default:
        throw new IllegalArgumentException("invalid op " + op.get().toString());
    }
  }

  /**
   * Send a message up the tree that you have completed the current sub-child.
   * You may need to update units as you go.
   * @param childExpr - the child expression returned when a subtree reported done.
   * @return an expr builder if the expr is incomplete or an expr if it isn't.
   */
  public Either<ExprBuilder, Expr> done(Expr childExpr) {
    // Make sure this has been filled up.
    assert op.isPresent();

    // Update units based on received childExpr
    if (op.get().isTuple()) { // If a tuple, just propogate this tuple up.
      updateUnit(childExpr.unit);
      if (parent.isPresent()) {
        return parent.get().done(childExpr);
      } else {
        return Either.right(childExpr);
      }
    } else if (op.get().isUnit()) { // Perform a unit change and propogate up.
      assert (!leftArg.isPresent());
      leftArg = Optional.of(childExpr);
      // Update types
      Expr expr = Expr.unitChange(leftArg.get(), unitArg.get());
      if (parent.isPresent()) {
        return parent.get().done(expr);
      } else {
        return Either.right(expr);
      }
    } else {
      if (!leftArg.isPresent()) { // If the right arg needs to be processed, do that, otherwise, propogate up.
        leftArg = Optional.of(childExpr);
        // Update types
        updateChildrenAfterLeftDone();
        return Either.left(rightArgBuilder.get());
      }
      else {
        rightArg = Optional.of(childExpr);
        updateChildrenAfterRightDone();
        // Complete expression.
        Expr expr = Expr.op(op.get(), leftArg.get(), rightArg.get());
        if (parent.isPresent())
          return parent.get().done(expr);
        else
          return Either.right(expr);
      }
    }
  }

  public Either<ExprBuilder, Expr> tuple(NumericTuple tuple) {
    assert expectedUnit.map(u -> u.equals(tuple.unit)).orElse(true);
    op = Optional.of(Operation.TUPLE);
    tupleArg = Optional.of(tuple);
    return done(Expr.tuple(tuple));
  }

  public ExprBuilder unitChange(UnitTuple u) {
    assert !leftArgBuilder.isPresent() && !rightArgBuilder.isPresent(); // op hasn't already been called.

    this.op = Optional.of(Operation.UNIT);
    unitArg = Optional.of(u);
    leftArgBuilder = Optional.of(new ExprBuilder(Optional.of(this), expectedValue.map(v -> v / u.val), expectedUnit.map(u_ -> u_.convert(u.symmetric())))); // Change arguments because I'm "reverse-applying" the unit change.
    return leftArgBuilder.get();
  }

  public ExprBuilder op(Operation op) {
    assert op.isOp();
    assert !leftArgBuilder.isPresent() && !rightArgBuilder.isPresent(); // op hasn't already been called.

    this.op = Optional.of(op);
    switch(op) {
      case ADD:
        // Arguments should be of same type (if type has been determined).
        leftArgBuilder = Optional.of(new ExprBuilder(Optional.of(this), Optional.empty(), expectedUnit));
        rightArgBuilder = Optional.of(new ExprBuilder(Optional.of(this), Optional.empty(), expectedUnit));
        break;
      case MUL:
      case DIV:
        // Arguments should be of same type (if type has been determined).
        leftArgBuilder = Optional.of(new ExprBuilder(Optional.of(this), Optional.empty(), Optional.empty()));
        rightArgBuilder = Optional.of(new ExprBuilder(Optional.of(this), Optional.empty(), Optional.empty()));
        break;
      default:
        throw new IllegalArgumentException("invalid operator: " + op.toString());
    }
    return leftArgBuilder.get();
  }

  public ExprBuilder mul() {
    return op(Operation.MUL);
  }
  public ExprBuilder add() {
    return op(Operation.ADD);
  }
  public ExprBuilder div() {
    return op(Operation.DIV);
  }




}
