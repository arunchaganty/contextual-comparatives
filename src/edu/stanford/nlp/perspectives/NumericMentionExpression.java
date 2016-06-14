package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.Pair;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static edu.stanford.nlp.perspectives.NumericTuple.*;
import static edu.stanford.nlp.util.logging.Redwood.Util.log;
import static edu.stanford.nlp.util.logging.Redwood.Util.logf;

/**
 * Stores a mention + expression (for future featurization)
 * TODO: This should be a Mention + an Expression.
 */
public class NumericMentionExpression {
  public final int id;
  public final int mention_id;
  public final Sentence mention_sentence;
  public final double mention_value;
  public final Unit mention_unit;
  public final double mention_normalized_value;
  public final Unit mention_normalized_unit;
  public final int mention_token_begin;
  public final int mention_token_end;
  public final int expression_id;
  public final double expression_value;
  public final Unit expression_unit;
  public final double expression_multiplier;
  public final Expr expression;
  public final int response_id;
  public final Sentence response;
  public final Optional<Double> label;

  public final NumericMention mention;

  public NumericMentionExpression(int id, int mention_id, Sentence mention_sentence, double mention_value, Unit mention_unit, double mention_normalized_value, Unit mention_normalized_unit, int mention_token_begin, int mention_token_end, int expression_id, double expression_value, Unit expression_unit, double expression_multiplier, Expr expression, int response_id, Sentence response, Optional<Double> label) {
    this.id = id;
    this.mention_id = mention_id;
    this.mention_sentence = mention_sentence;
    this.mention_value = mention_value;
    this.mention_unit = mention_unit;
    this.mention_normalized_value = mention_normalized_value;
    this.mention_normalized_unit = mention_normalized_unit;
    this.mention_token_begin = mention_token_begin;
    this.mention_token_end = mention_token_end;
    this.expression_id = expression_id;
    this.expression_value = expression_value;
    this.expression_unit = expression_unit;
    this.expression_multiplier = expression_multiplier;
    this.expression = expression;
    this.response_id = response_id;
    this.response = response;
    this.label = label;
    this.mention = new NumericMention(mention_id, mention_value, mention_unit, mention_normalized_value, mention_normalized_unit, mention_sentence.sentenceid().toString(), 0, 0, mention_token_begin, mention_token_end, mention_sentence);
  }


  public static List<NumericMentionExpression> readFromTSV(String filename) throws FileNotFoundException {
    RecordIterator rit = new RecordIterator(filename, "\t");
    List<String> header = rit.next();
    assert header.equals(Arrays.asList("id","mention_id","mention_sentence","mention_value","mention_unit","mention_normalized_value","mention_normalized_unit","mention_token_begin","mention_token_end","expression_id","expression_value","expression_unit","expression","response_id","response","label"));

    List<NumericMentionExpression> values = new ArrayList<>();
    while(rit.hasNext()) {
      List<String> record = rit.next();
      int id = Integer.parseInt(record.get(0));
      int mention_id = Integer.parseInt(record.get(1));
      Sentence mention_sentence = new Sentence(record.get(2));
      double mention_value = Double.parseDouble(record.get(3));
      Unit mention_unit = Unit.of(record.get(4));
      double mention_normalized_value = Double.parseDouble(record.get(5));
      Unit mention_normalized_unit = Unit.of(record.get(6));
      int mention_token_begin = Integer.parseInt(record.get(7));
      int mention_token_end = Integer.parseInt(record.get(8));
      int expression_id = Integer.parseInt(record.get(9));
      double expression_value = Double.parseDouble(record.get(10));
      Unit expression_unit = Unit.of(record.get(11));
      Pair<Double, Expr> pair = Expr.ofWithMultiplier(record.get(12));
      double expression_multiplier = pair.first;
      Expr expression = pair.second;
      int response_id = Integer.parseInt(record.get(13));
      Sentence response = new Sentence(record.get(14));
      double label = Double.parseDouble(record.get(15));

      values.add(new NumericMentionExpression(id, mention_id, mention_sentence, mention_value, mention_unit, mention_normalized_value, mention_normalized_unit, mention_token_begin, mention_token_end, expression_id, expression_value, expression_unit, expression_multiplier, expression, response_id, response, Optional.of(label)));
    }

    log("Done.");
    return values;
  }

  public static NumericMentionExpression of(NumericMention mention, Expr expr) {
    return new NumericMentionExpression(0,
        mention.id.orElse(0), mention.sentence.get(), mention.value, mention.unit, mention.normalized_value, mention.normalized_unit, mention.token_begin, mention.token_end,
        0, expr.value, expr.unit, mention.value / expr.value, expr, 0, new Sentence(expr.toString()), Optional.empty());
  }

  public String toHumanString() {
    return expression.toHumanString(expression_multiplier);
  }
}
