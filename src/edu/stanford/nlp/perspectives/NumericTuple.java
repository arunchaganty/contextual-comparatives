package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.Expr.Operation;
import edu.stanford.nlp.simple.Sentence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static edu.stanford.nlp.util.logging.Redwood.Util.logf;

/**
 * Represents a numeric tuple (s, p, v, u):
 *    - subject, predicate, value unit.
 * Default to-string is "(s, p, v, u)"
 */
public class NumericTuple {
  public final Optional<Integer> id;
  public final String subj;
  public final String pred;
  public final double val;
  public final Unit unit;
  public final Sentence subjSentence;

  public static class Unit {
    public final List<String> numerator;
    public final List<String> denominator;

    public Unit(List<String> numerator, List<String> denominator) {
      this.numerator = numerator.stream().filter(s -> s.length() > 0).collect(Collectors.toList());
      this.denominator = denominator.stream().filter(s -> s.length() > 0).collect(Collectors.toList());

      Collections.sort(numerator);
      Collections.sort(denominator);
    }

    public boolean isPrimary() {
      return numerator.size() == 1 && denominator.size() == 0;
    }

    // Everything prefaced by 'per' is treated as denominator.
    public static Unit of(String unit) {
      List<String> numerator = new ArrayList<>();
      List<String> denominator = new ArrayList<>();

      String[] tokens = unit.split("\\s+");
      boolean denom = false;
      for(String tok : tokens) {
        if (tok.equals("per")) {
          denom = true;
        } else {
          if(denom)
            denominator.add(tok);
          else
            numerator.add(tok);

          denom = false;
        }
      }
      return new Unit(numerator, denominator);
    }

    public static Unit op(Operation op, Unit some, Unit other) {
      switch(op) {
        case ADD:
          assert some.equals(other);
          return some;
        case MUL:
          return mul(some, other);
        case DIV:
          return div(some, other);
        default:
          throw new IllegalArgumentException("Not a valid operation: " + op);
      }
    }

    public Unit op(Operation op, Unit other) {
      return op(op, this, other);
    }

    public static Unit mul(Unit some, Unit other) {
      List<String> numerator = new ArrayList<>(some.numerator);
      List<String> denominator = new ArrayList<>(some.denominator);

      for(String unit : other.numerator) {
        if (denominator.contains(unit)) {
          denominator.remove(unit);
        } else {
          numerator.add(unit);
        }
      }
      for(String unit : other.denominator) {
        if (numerator.contains(unit)) {
          numerator.remove(unit);
        } else {
          denominator.add(unit);
        }
      }

      return new Unit(numerator, denominator);
    }

    public static boolean canMul(Unit some, Unit other) {
      return other.numerator.stream().anyMatch(some.denominator::contains);
    }

    public boolean canMul(Unit other) {
      return canMul(this, other);
    }

    public Unit mul(Unit other) {
      return mul(this, other);
    }

    public static Unit invert(Unit some) {
      return new Unit(some.denominator, some.numerator);
    }
    public Unit invert() {
      return invert(this);
    }

    public static Unit div(Unit some, Unit other) {
      return mul(some, invert(other));
    }
    public Unit div(Unit other) {
      return div(this, other);
    }

    public static Unit convert(Unit some, UnitTuple other) {
      return div(mul(some, other.finalUnit), other.originalUnit);
    }
    public Unit convert(UnitTuple other) {
      return convert(this, other);
    }

      @Override
    public String toString() {
      StringBuilder builder = new StringBuilder();
      for(String tok : numerator)
        builder.append(tok).append(" ");
      for(String tok : denominator)
        builder.append("per ").append(tok).append(" ");
      return builder.toString().trim();
    }

    @Override
    public int hashCode() {
      return toString().hashCode();
    }

    @Override
    public boolean equals(Object other_) {
      if(other_ instanceof Unit) {
        Unit other = (Unit) other_;
        return other.numerator.equals(numerator) && other.denominator.equals(denominator);
      }
      return false;
    }

    public boolean contains(Unit other) {
      return this.numerator.containsAll(other.numerator) && this.denominator.containsAll(other.denominator);
    }

    public static Unit empty() {
      return new Unit(Collections.emptyList(), Collections.emptyList());
    }
  }

  public NumericTuple(Optional<Integer> id, String subj, String pred, double val, Unit unit) {
    this.id = id;
    this.subj = subj;
    this.pred = pred;
    this.val = val;
    this.unit = unit;
    this.subjSentence = new Sentence(subj);
  }
  public NumericTuple(String subj, String pred, double val, String unit) {
    this(Optional.empty(), subj, pred, val, Unit.of(unit));
  }
  public NumericTuple(Integer id, String subj, String pred, double val, String unit) {
    this(Optional.of(id), subj, pred, val, Unit.of(unit));
  }
  public static NumericTuple of(String subj, String pred, double val, String unit) {
    return new NumericTuple(Optional.empty(), subj, pred, val, Unit.of(unit));
  }

  // Format of tuple is [value unit (name)]
  public static NumericTuple of(String part) {
    Pattern pat = Pattern.compile("([0-9]+(\\.[0-9]+)?) ([^(]+) \\(([^)]+)\\)");

    Matcher matcher = pat.matcher(part);
    assert matcher.matches();
    double value = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(3);
    String name = matcher.group(4);
    return NumericTuple.of(name, "is", value, unit);
  }

  @Override
  public String toString() {
    // TODO(chaganty): handle making value human readable.
    // TODO(chaganty): handle plurality in units.
    if(id.isPresent())
      return String.format("%d: %s %s %.2e %s", id.get(), subj, pred, val, unit);
    else
      return String.format("%s %s %.2e %s", subj, pred, val, unit);
  }

  public NumericTuple add(final NumericTuple other) {
//    assert other.pred.equals(pred); // TODO(chaganty): is this even useful?
    assert other.unit.equals(unit);
    return NumericTupleBuilder.of(this)
        .subj(subj + " and " + other.subj)
        .val(val + other.val)
        .build();
  }

  public NumericTuple mul(final NumericTuple other) {
    return NumericTupleBuilder.of(this)
        .subj(subj)
        .pred(" for the " + other.unit + " " + other.subj + " " + other.pred + " " + pred)
        .val(val * other.val)
        .unit(unit.mul(other.unit))
        .build();
  }

  public NumericTuple div(final NumericTuple other) {
    return NumericTupleBuilder.of(this)
        .subj(subj)
        .pred(pred + " for every " + other.unit + " " + other.subj + " " + other.pred)
        .val(val / other.val)
        .build();
  }

  public NumericTuple unitChange(UnitTuple conversion) {
    return NumericTupleBuilder.of(this)
        .val(val * conversion.val)
        .unit(unit.convert(conversion))
        .build();
  }

  public NumericTuple unitChange(String oldUnit, double multiplier, String newUnit) {
    return unitChange(new UnitTuple(Unit.of(oldUnit), multiplier, Unit.of(newUnit)));
  }

  public String comparisonNP(double value, Unit unit) {
    assert this.unit.equals(unit);
    double multiplier = value / this.val;
    if (multiplier > 0.8)
      multiplier = Math.round(multiplier);
    if (multiplier == 1)
      return String.format("as many %s as %s %s.", unit, subj, pred);
    else if (multiplier > 1)
      return String.format("as many %s as %d times how much %s %s.", unit, (int) multiplier, subj, pred);
    else {
      long numerator = 1; long denominator = 1;
      if (multiplier > 0.8) assert false;
      else if (multiplier > 0.6) {numerator = 3; denominator = 4;}
      else if (multiplier > 0.35) {numerator = 1; denominator = 2;}
      else if (multiplier > 0.15) {numerator = 1; denominator = 4;}
      else if (multiplier > 0.05) {numerator = 1; denominator = 10;}
      else {numerator = 1; denominator = (int) Math.round(1/multiplier);}
      return String.format("as many %s as %d/%d th how much %s %s.", unit, numerator, denominator, subj, pred);
    }
  }
  public String comparisonNP(NumericMention mention) {
    return comparisonNP(mention.value, mention.unit);
  }
  public String comparisonNP(NumericTuple tuple) {
    return comparisonNP(tuple.val, tuple.unit);
  }

  public String compare(NumericTuple e2) {
    assert unit.equals(e2.unit);
    return String.format("%s %s as many %s as %s %s.", subj, pred, unit, e2.subj, e2.pred);
  }

  public String compare(double multiplier, NumericTuple e2) {
    assert unit.equals(e2.unit);
    return String.format("%s %s as many %s as %s times how much %s %s.", subj, pred, unit, multiplier, e2.subj, e2.pred);
  }

  public String comparePercent(double percent, NumericTuple e2) {
    return String.format("%s %s as many %s as %s%% of how much %s %s.", subj, pred, unit, percent * 100, e2.subj, e2.pred);
  }

  @Override
  public boolean equals(Object other_) {
    if(other_ instanceof  NumericTuple) {
      NumericTuple other = (NumericTuple) other_;
      return subj.equals(other.subj) &&
          pred.equals(other.pred) &&
          (Math.abs(val - other.val)/Math.abs(val) < 1e-3) &&
          unit.equals(other.unit);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}
