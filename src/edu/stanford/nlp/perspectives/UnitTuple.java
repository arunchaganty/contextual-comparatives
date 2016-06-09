package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * A unit tuple is a special numeric tuple
 */
public class UnitTuple extends NumericTuple {
  public final Unit originalUnit;
  public final double val;
  public final Unit finalUnit;

  public UnitTuple(Unit originalUnit, double val, Unit finalUnit) {
    super(Optional.empty(), originalUnit.toString(), "is", val, finalUnit);

    this.originalUnit = originalUnit;
    this.val = val;
    this.finalUnit = finalUnit;
  }

  public static UnitTuple of(String oldUnit, double val, String newUnit) {
    return new UnitTuple(Unit.of(oldUnit), val, Unit.of(newUnit));
  }

  public static UnitTuple symmetric(UnitTuple tuple) {
    return new UnitTuple(tuple.finalUnit, 1./tuple.val, tuple.originalUnit);
  }
  public UnitTuple symmetric() {
    return symmetric(this);
  }

  public static UnitTuple invert(UnitTuple tuple) {
    return new UnitTuple(tuple.originalUnit.invert(), 1./tuple.val, tuple.finalUnit.invert());
  }
  public UnitTuple invert() {
    return invert(this);
  }


  @Override
  public String toString() {
    return Double.toString(val) + " " + finalUnit.div(originalUnit);
  }
}
