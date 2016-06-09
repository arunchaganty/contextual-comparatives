package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;

public class NumericMention {
  public final Optional<Integer> id;
  public final double value;
  public final Unit unit;

  public final double normalized_value;
  public final Unit normalized_unit;

  public final String sentence_id;
  public final int doc_char_begin;
  public final int doc_char_end;
  public final int token_begin;
  public final int token_end;

  public final Optional<Sentence> sentence;

  public NumericMention(Optional<Integer> id, double value, Unit unit, double normalized_value, Unit normalized_unit, String sentence_id, int doc_char_begin, int doc_char_end, int token_begin, int token_end, Optional<Sentence> sentence) {
    this.id = id;
    this.value = value;
    this.unit = unit;
    this.normalized_value = normalized_value;
    this.normalized_unit = normalized_unit;
    this.sentence_id = sentence_id;
    this.doc_char_begin = doc_char_begin;
    this.doc_char_end = doc_char_end;
    this.token_begin = token_begin;
    this.token_end = token_end;
    this.sentence = sentence;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NumericMention) {
      NumericMention other = (NumericMention) obj;
      if(id.isPresent() && other.id.isPresent()) return Objects.equals(id.get(), other.id.get());
      else if (sentence.isPresent() && other.sentence.isPresent()) {
        return sentence.get().toString().equals(other.sentence.get().toString()) &&
            token_begin == other.token_begin &&
            token_end == other.token_end;
      } else {
        return super.equals(obj);
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, sentence, token_begin, token_end);
  }

  public static NumericMention of(double value, String unit) {
    return new NumericMention(Optional.empty(), value, Unit.of(unit), value, Unit.of(unit), "", 0, 0, 0, 0, Optional.empty());
  }
  public static NumericMention of(double value, String unit, String sentence) {
    return new NumericMention(Optional.empty(), value, Unit.of(unit), value, Unit.of(unit), "", 0, 0, 0, 0, Optional.of(new Sentence(sentence)));
  }
  public NumericMention unitChange(UnitTuple tuple) {
    return new NumericMention(id, value * tuple.val, unit.convert(tuple), normalized_value, normalized_unit, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end, sentence);
  }

  public NumericMention(double value, Unit unit,  double normalized_value, Unit normalized_unit, String sentence_id, int doc_char_begin, int doc_char_end, int token_begin, int token_end) {
    this(Optional.empty(), value, unit, normalized_value, normalized_unit, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end, Optional.empty());
  }

  public NumericMention(double value, Unit unit,  String sentence_id, int doc_char_begin, int doc_char_end, int token_begin, int token_end) {
    this(Optional.empty(), value, unit, value, unit, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end, Optional.empty());
  }

  public NumericMention(int id, double value, Unit unit, double normalized_value, Unit normalized_unit, String sentence_id, int doc_char_begin, int doc_char_end, int token_begin, int token_end, Sentence sentence) {
    this(Optional.of(id), value, unit, normalized_value, normalized_unit, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end, Optional.of(sentence));
  }

  public NumericMention(int id, double value, Unit unit, String sentence_id, int doc_char_begin, int doc_char_end, int token_begin, int token_end, Sentence sentence) {
    this(Optional.of(id), value, unit, value, unit, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end, Optional.of(sentence));
  }

  public static NumericMention empty() {
    return new NumericMention(Optional.empty(), 0., Unit.empty(), 0., Unit.empty(), "", 0, 0, 0, 0, Optional.empty());
  }

  public String name() {
    if (!sentence.isPresent()) return "(n/a)";
    else {
      StringBuilder ret = new StringBuilder();
      for(int i = token_begin; i < token_end; i++) {
        ret.append(sentence.get().word(i)).append(" ");
      }
      return ret.toString();
    }
  }

  @Override
  public String toString() {
    return String.format("%s %.2e", unit, value);
  }

  public String toTSV() {
    return String.join("\t", String.valueOf(value), unit.toString(), sentence_id, String.valueOf(doc_char_begin), String.valueOf(doc_char_end), String.valueOf(token_begin), String.valueOf(token_end));
  }

  public List<String> toList() {
    return new ArrayList<>(Arrays.asList(String.valueOf(value), unit.toString(), sentence_id, String.valueOf(doc_char_begin), String.valueOf(doc_char_end), String.valueOf(token_begin), String.valueOf(token_end)));
  }

  public static boolean valueEquals(NumericMention some, NumericMention other) {
    return Math.abs(some.value - other.value) < 1e-6 && some.unit.equals(other.unit);
  }
  public boolean valueEquals(NumericMention other) {
    return valueEquals(this, other);
  }
}

