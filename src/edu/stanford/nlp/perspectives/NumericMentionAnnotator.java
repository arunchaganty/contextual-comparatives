package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.perspectives.NumericTuple.Unit;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.TokenSequenceMatcher;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;

import static edu.stanford.nlp.util.logging.Redwood.Util.warn;

/**
 * Annotate numeric mentions that match a class of simple tokensregex formats.
 * TODO: extend annotator
 */
public class NumericMentionAnnotator {
  public NumericMentionAnnotator() {}

  protected static Env getNewEnv() {
    Env env = TokenSequencePattern.getNewEnv();
    env.bind("$NUMBER", "/[0-9]+(,[0-9]+)?(\\.[0-9]+)?/");
    env.bind("$MULTIPLIER", "/(hundred)|(thousand)|(million)|(billion)|(trillion)/");
    env.bind("$MONEY", "[{word:/(USD)|(US\\$)|(\\$)/}]");
    env.bind("$WEIGHT", "/((gram)|(kilogram)|(ounce)|(pound)|(ton)|(gm)|(kg)|(oz)|(lb))\\.?s?/");
    env.bind("$LENGTH", "/(((millimeter)|(centimeter)|(meter)|(kilometer)|(inch)|(inche)|(foot)|(feet)|(yard)|(mile))s?)|((mm)|(cm)|(m)|(km)|(ft)|(yd)|(mi))\\.?s?/"); // (in)|
    env.bind("$TIME", "/((millisecond)|(second)|(minute)|(hour)|(day)|(week)|(day)|(year))s?/");
    env.bind("$AREAMOD", "/(square)|(sq\\.?)/");
    env.bind("$AREAEXT", "/(hectacre)|(acre)|(ha\\.)|(ac\\.)/");
    env.bind("$VOLUMEMOD", "/(cubic)|(cu\\.?)/");
    env.bind("$VOLUMEEXT", "/(gallon)|(ga\\.?)|(liter)|(l)|(barrel)|(quart)|(qt\\.?)/");
    env.bind("$PEOPLE", "/(people)|(person)/");
    env.bind("$CAR", "/cars?/");
    env.bind("$GUN", "/guns?/");
    return env;
  }

  public static final Map<String, Double> MULTIPLIERS = new HashMap<String, Double>() {{
    put("hundred", 1e2);
    put("hundreds", 1e2);
    put("thousand", 1e3);
    put("thousands", 1e3);
    put("million", 1e6);
    put("millions", 1e6);
    put("billion", 1e9);
    put("billions", 1e9);
  }};

  protected static final TokenSequencePattern MONEY_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($MONEY) ($NUMBER) ($MULTIPLIER)*");
  protected static final TokenSequencePattern TIME_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* ($TIME)");
  protected static final TokenSequencePattern WEIGHT_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* ($WEIGHT)");
  protected static final TokenSequencePattern LENGTH_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* ($LENGTH)");
  protected static final TokenSequencePattern AREA_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* (($AREAMOD $LENGTH) | $AREAEXT)");
  protected static final TokenSequencePattern VOLUME_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* (($VOLUMEMOD /./? $LENGTH) | $VOLUMEEXT)");
  protected static final TokenSequencePattern ENTITY_PATTERN = TokenSequencePattern.compile(getNewEnv(), "($NUMBER) ($MULTIPLIER)* (($PEOPLE | $CAR | $GUN))");

  public List<NumericMention> annotate(final Sentence sentence) {
    List<NumericMention> mentions = new ArrayList<>();
    mentions.addAll(sentence.find(MONEY_PATTERN, m -> fromMatcher(m, sentence, 2, 3, 1)));
    mentions.addAll(sentence.find(WEIGHT_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    mentions.addAll(sentence.find(TIME_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    mentions.addAll(sentence.find(LENGTH_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    mentions.addAll(sentence.find(AREA_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    mentions.addAll(sentence.find(VOLUME_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    mentions.addAll(sentence.find(ENTITY_PATTERN, m -> fromMatcher(m, sentence, 1, 2, 3)));
    return mentions;
  }


  /**
   * Reads entries from matcher.
   * @param matcher -- matcher (must guarantee that matches() applise.
   * @param sentence
   */
  public NumericMention fromMatcher(final TokenSequenceMatcher matcher, final Sentence sentence, final int valuePos, final int multiplierPos, final int unitPos) {
    List<CoreLabel> tokens = (List<CoreLabel>) matcher.groupInfo(0).nodes;

    int doc_char_begin = tokens.get(0).beginPosition();
    int doc_char_end = tokens.get(tokens.size()-1).endPosition();

    int token_begin = tokens.get(0).index();
    int token_end = tokens.get(tokens.size()-1).index();

    String unit = matcher.group(unitPos);
    String valueStr = matcher.group(valuePos).replace(",",""); // handle "300,000"
    double multiplier = 1.;

    if (matcher.group(multiplierPos) != null)
    { // Handle multiplier
      List<CoreLabel> mPrevTokens = (List<CoreLabel>) matcher.groupInfo(valuePos).nodes; // Use fact that multiplier always succeeds value
      List<CoreLabel> mTokens = (List<CoreLabel>) matcher.groupInfo(multiplierPos).nodes;
      int m_token_begin = mPrevTokens.get(mPrevTokens.size()-1).index() - token_begin +1;
      int m_token_end = mTokens.get(mTokens.size()-1).index() - token_begin;
      multiplier = tokens.subList(m_token_begin, m_token_end+1).stream()
          .map(t -> MULTIPLIERS.getOrDefault(t.word(), 1.))
          .reduce(multiplier, (v, v_) -> v * v_);
    }

    double value = Double.parseDouble(valueStr) * multiplier;

    // Handle square and cubic units
    unit = unit.replace(".", "")
        .replace("square ", "sq-")
        .replace("sq ", "sq-")
        .replace("cubic ", "cu-")
        .replace("cu ", "cu-");

    if (unit.split("\\s+").length != 1) {
      warn(unit);
    }
    assert unit.split("\\s+").length == 1;

    // Remove trailiing s
    if(unit.length() > 2 && unit.endsWith("s"))
      unit = unit.substring(0, unit.length()-1);

    return new NumericMention(Optional.empty(), value, Unit.of(unit), value, Unit.of(unit),sentence.sentenceid().orElse(""), doc_char_begin, doc_char_end, token_begin, token_end, Optional.of(sentence));
  }



}
