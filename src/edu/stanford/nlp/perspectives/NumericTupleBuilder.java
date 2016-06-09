package edu.stanford.nlp.perspectives;

import java.util.Optional;

/**
 * Created by chaganty on 2/1/16.
 */
public class NumericTupleBuilder {
  public Optional<Integer> id;
  public Optional<String> subj;
  public Optional<String> pred;
  public Optional<Double> val;
  public Optional<NumericTuple.Unit> unit;

  public NumericTupleBuilder() {
    subj = Optional.empty();
    pred = Optional.empty();
    val = Optional.empty();
    unit = Optional.empty();
  }

  public static NumericTupleBuilder of(NumericTuple tuple) {
    NumericTupleBuilder builder = new NumericTupleBuilder();
    builder.id = tuple.id;
    builder.subj(tuple.subj);
    builder.pred(tuple.pred);
    builder.val(tuple.val);
    builder.unit(tuple.unit);

    return builder;
  }

  public NumericTuple build() {
    assert subj.isPresent();
    assert pred.isPresent();
    assert val.isPresent();
    assert unit.isPresent();

    return new NumericTuple(id, subj.get(), pred.get(), val.get(), unit.get());
  }

  public NumericTupleBuilder id(Integer id) {
    this.id = Optional.of(id);
    return this;
  }

  public NumericTupleBuilder subj(String arg) {
    subj = Optional.of(arg);
    return this;
  }

  public NumericTupleBuilder pred(String arg) {
    pred = Optional.of(arg);
    return this;
  }

  public NumericTupleBuilder val(double arg) {
    val = Optional.of(arg);
    return this;
  }

  public NumericTupleBuilder unit(String arg) {
    unit = Optional.of(NumericTuple.Unit.of(arg));
    return this;
  }

  public NumericTupleBuilder unit(NumericTuple.Unit arg) {
    unit = Optional.of(arg);
    return this;
  }
}
