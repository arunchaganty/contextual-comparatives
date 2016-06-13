package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Represents a (full) expression tree.
 */
public class Expr {

  public enum Operation {
    ADD("+"),
    MUL("*"),
    DIV("/"),
    TUPLE("t"),
    UNIT("u");

    String repr;
    Operation(String s) { repr = s; }
    @Override
    public String toString() { return repr; }

    boolean isOp() {
      switch(this) {
        case ADD:
        case MUL:
        case DIV:
          return true;
        default:
          return false;
      }
    }
    boolean isTuple() { return this == TUPLE; }
    boolean isUnit() { return this == UNIT; }
  }

  public final Optional<Expr> parent; // To traverse the expression tree.
  public final int depth;

  public final Operation op;
  public final Optional<Expr> leftArg;
  public final Optional<Expr> rightArg;
  public final Optional<NumericTuple> tupleArg;
  public final Optional<UnitTuple> unitArg;

  public final Double value;
  public final Unit unit;

  public Expr(Optional<Expr> parent, Operation op, Optional<Expr> leftArg, Optional<Expr> rightArg, Optional<NumericTuple> tupleArg, Optional<UnitTuple> unitArg, Double value, Unit unit) {
    this.parent = parent;
    depth = parent.map(p -> p.depth + 1).orElse(0);
    assert depth < 10;
    this.op = op;
    this.leftArg = leftArg.map(e -> e.asChildOf(this));
    this.rightArg = rightArg.map(e -> e.asChildOf(this));
    this.tupleArg = tupleArg;
    this.unitArg = unitArg;
    this.value = value;
    this.unit = unit;
  }

  @Override
  public String toString() {
    switch (op) {
      case TUPLE:
        return tupleArg.get().toString();
      case UNIT:
        return String.format("(%s *u %s)", leftArg.get(), unitArg.get());
      default:
        return String.format("(%s %s %s)", leftArg.get(), op, rightArg.get());
    }
  }

  public NumericTuple evaluate() {
    switch (op) {
      case ADD:
        return leftArg.get().evaluate().add(rightArg.get().evaluate());
      case MUL:
        return leftArg.get().evaluate().mul(rightArg.get().evaluate());
      case DIV:
        return leftArg.get().evaluate().div(rightArg.get().evaluate());
      case TUPLE:
        return tupleArg.get();
      case UNIT:
        return leftArg.get().evaluate().unitChange(unitArg.get());
      default:
        throw new IllegalArgumentException("not a valid argument operation for a numeric expression: " + op.name());
    }
  }

  public static Expr tuple(NumericTuple t) {
    return new Expr(Optional.empty(), Operation.TUPLE, Optional.empty(), Optional.empty(), Optional.of(t), Optional.empty(), t.val, t.unit);
  }
  public static Expr unitChange(Expr expr, UnitTuple u) {
    return new Expr(Optional.empty(), Operation.UNIT, Optional.of(expr), Optional.empty(), Optional.empty(), Optional.of(u), expr.value * u.val, expr.unit.convert(u));
  }
  public static Expr op(Operation op, Expr e1, Expr e2) {
    assert op != Operation.TUPLE;
    double value;
    switch(op) {
      case ADD:
        value = e1.value + e2.value;
        break;
      case MUL:
        value = e1.value * e2.value;
        break;
      case DIV:
        value = e1.value / e2.value;
        break;
      default:
        value = 0;
        assert false;
    }

    return new Expr(Optional.empty(), op, Optional.of(e1), Optional.of(e2), Optional.empty(), Optional.empty(), value, e1.unit.op(op, e2.unit));
  }

  // Make this expr a child of another expression
  public Expr asChildOf(Expr parent) {
    return new Expr(Optional.of(parent), op, leftArg, rightArg, tupleArg, unitArg, value, unit);
  }

  /** Collect all numeric arguments **/
  public void arguments(List<NumericTuple> agg) {
    switch(op) {
      case ADD:
      case MUL:
      case DIV:
        leftArg.get().arguments(agg);
        rightArg.get().arguments(agg);
        break;
      case TUPLE:
        agg.add(tupleArg.get());
        break;
      case UNIT:
        leftArg.get().arguments(agg);
        break;
    }
  }

  /** Collect all numeric arguments **/
  public List<NumericTuple> arguments() {
    List<NumericTuple> args = new ArrayList<>();
    arguments(args);
    return args;
  }

  // Format of expression is [value unit (name)] [* value unit (name)]+
  public static Expr of(String s) {
    Pattern exprPat = Pattern.compile("([0-9]+(\\.[0-9]+)?) ([^(]+) \\(([^)]+)\\)");
    Optional<Expr> ret = Optional.empty();

    String[] parts = s.split("\\*");
    for(int i = 0; i < parts.length; i++) {
      String part = parts[i].trim();
      NumericTuple t = NumericTuple.of(part);
      Expr e = Expr.tuple(t);
      if (ret.isPresent()) {
        ret = Optional.of(Expr.op(Operation.MUL, ret.get(), e));
      } else {
        ret = Optional.of(e);
      }
    }

    return ret.get();
  }

  public static Pair<Double, Expr> ofWithMultiplier(String s) {
    Pattern exprPat = Pattern.compile("([0-9]+(.[0-9]+)) ([^(]+) \\(([^)]+)\\)");
    Optional<Expr> ret = Optional.empty();

    String[] parts = s.split("\\*");
    double multiplier = Double.parseDouble(parts[0]);
    for(int i = 1; i < parts.length; i++) {
      String part = parts[i].trim();
      NumericTuple t = NumericTuple.of(part);
      Expr e = Expr.tuple(t);
      if (ret.isPresent()) {
        ret = Optional.of(Expr.op(Operation.MUL, ret.get(), e));
      } else {
        ret = Optional.of(e);
      }
    }

    return Pair.makePair(multiplier,ret.get());
  }
}
