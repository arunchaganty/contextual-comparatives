package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.util.Pair;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static edu.stanford.nlp.perspectives.NumericTuple.*;

/**
 * A set of unit tuples
 */
public class UnitDatabase extends HashSet<UnitTuple> {
  public UnitDatabase() {
    super();
  }

  public Pair<Double, String> standardizeUnits(double value, String unit_) {
    Pair<Double, Unit> pair = standardizeUnits(value, Unit.of(unit_));
    return Pair.makePair(pair.first, pair.second.toString());
  }

  public Pair<Double, Unit> standardizeUnits(double value, Unit unit) {
    for(UnitTuple tuple : this) {
      if (unit.contains(tuple.originalUnit)) {
        unit = unit.convert(tuple);
        value = value * tuple.val;
      }
    }
    return Pair.makePair(value, unit);
  }

  public NumericTuple standardizeUnits(NumericTuple in) {
    for(UnitTuple tuple : this) {
      if (in.unit.contains(tuple.originalUnit)) {
        in = in.unitChange(tuple);
      }
    }
    return in;
  }
  public NumericMention standardizeUnits(NumericMention in) {
    for(UnitTuple tuple : this) {
      if (in.unit.contains(tuple.originalUnit)) {
        in = in.unitChange(tuple);
      }
    }
    return in;
  }

  @Override
  public boolean add(UnitTuple entry) {
    boolean added = super.add(entry);
    added = super.add(entry.invert()) || added;
    return added;
  }

  public static UnitDatabase readFromTSV(String filename) {
    UnitDatabase db = new UnitDatabase();
    RecordIterator rit = null;
    try {
      rit = new RecordIterator(filename, "\t");
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    List<String> header = rit.next();
    assert header.equals(Arrays.asList("unit", "conversion", "fundamental_unit"));

    while(rit.hasNext()) {
      List<String> entries = rit.next();
      UnitTuple tuple = new UnitTuple(
          Unit.of(entries.get(0)),            // original unit
          Double.parseDouble(entries.get(1)), // value
          Unit.of(entries.get(2))             // original unit
      );
      db.add(tuple);
    }
    return db;
  }
}
