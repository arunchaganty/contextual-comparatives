package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.simple.Sentence;

import java.util.Optional;

/**
 * Some default mentions to play around with.
 */
public class NumericMentionTest {
  public static final NumericMention nasaBudget =
      new NumericMention(Optional.empty(),
          17e9, Unit.of("US$"),
          17e9, Unit.of("US$"),
          "",
          0,0,0,0,
          Optional.of(new Sentence("The NASA budget is US $17 billion.")));

  public static final NumericMention usCars =
      new NumericMention(Optional.empty(),
          250e6, Unit.of("car"),
          250e6, Unit.of("car"),
          "",
          0,0,0,0,
          Optional.of(new Sentence("The United States has 250 million cars.")));

  public static final NumericMention worldGas =
      new NumericMention(Optional.empty(),
          584e9, Unit.of("gallon per year"),
          584e9, Unit.of("gallon per year"),
          "",
          0,0,0,0,
          Optional.of(new Sentence("The world produces 584 trillion gallons of gas a year.")));
}
