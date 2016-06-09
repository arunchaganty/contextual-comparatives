package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.authordisambig.util.StopWords;
import edu.stanford.nlp.ling.Datum;
import edu.stanford.nlp.ling.RVFDatum;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.stats.Counters;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Created by chaganty on 3/11/16.
 */
public class Featurizer {
  private final Util.WordEmbedding embeddings;
  private final List<BiConsumer<Counter<String>, NumericMentionExpression>> featurizers;
  private final HashSet<String> stopwords = new StopWords() {{
    add(".");
    add(",");
    add(";");
    add("-lrb-");
    add("-rrb-");
    add("-lsb-");
    add("-rsb-");
    add("--");
    add("``");
    add("''");
  }};

  public Featurizer(Util.WordEmbedding embeddings, String featureTemplates) {
    this.featurizers = getFeaturizers(featureTemplates);
    this.embeddings = embeddings;
  }

  @Deprecated
  protected void semanticSimilarity(Counter<String> features, String prefix, Sentence str1, Sentence str2) {
    Counter<String> v1 = new ClassicCounter<>(str1.lemmas().stream().map(String::toLowerCase).collect(Collectors.toList()));
    Counter<String> v2 = new ClassicCounter<>(str2.lemmas());
    // Remove any stopwords.
    for(String word : stopwords) {
      v1.remove(word);
      v2.remove(word);
    }
    // take inner product.
    double sim = Counters.dotProduct(v1, v2) / (Counters.saferL2Norm(v1) * Counters.saferL2Norm(v2));
    features.incrementCount(prefix + "semantic-similarity", 2*sim - 1); // to make it between 0 and 1.
  }

  protected double semanticSimilarityVW(Counter<String> features, String prefix, NumericMention mention, Sentence candidate) {
    double[] v1 = embeddings.get(mention.sentence.get(), mention.token_begin, mention.token_end);
    double[] v2 = embeddings.get(candidate);
    double sim = Util.WordEmbedding.dotProduct(v1, v2);
//    double sim =
//        candidate.lemmas().stream()
//            .map(w -> embeddings.get(Collections.singletonList(w), 0, 1))
//            .map(v2 -> Util.WordEmbedding.dotProduct(v1, v2))
//            .max(Double::compare)
//            .get();
//    features.incrementCount(prefix + "semantic-similarity", 2*sim - 1); // to make it between 0 and 1.
    return 2*sim-1;
  }

  protected double semanticSimilarityVW(Counter<String> features, String prefix, Sentence candidate1, Sentence candidate2) {
    double[] v1 = embeddings.get(candidate1);
    double[] v2 = embeddings.get(candidate2);
    double sim = Util.WordEmbedding.dotProduct(v1, v2);
//    features.incrementCount(prefix + "semantic-similarity", 2*sim - 1); // to make it between 0 and 1.
    return 2*sim - 1;
  }

  protected void semanticFeatures(Counter<String> features, String prefix, Sentence str) {
    double[] vec = embeddings.get(str);
    for(int i = 0; i < vec.length; i++)
      features.incrementCount(prefix + "wv-" + i, vec[i]);
  }
  protected void semanticFeatures(Counter<String> features, String prefix, NumericMention mention) {
    double[] vec = embeddings.get(mention.sentence.get(), mention.token_begin, mention.token_end);
    for(int i = 0; i < vec.length; i++)
      features.incrementCount(prefix + "wv-" + i, vec[i]);
  }

  protected void semanticCrossFeatures(Counter<String> features, String prefix, Sentence str1, Sentence str2) {
    double[] vec1 = embeddings.get(str1);
    double[] vec2 = embeddings.get(str2);
    for(int i = 0; i < vec1.length; i++)
      features.incrementCount(prefix + "wv-" + i, vec1[i] - vec2[i]);
  }

  protected void semanticCrossFeatures(Counter<String> features, String prefix, NumericMention mention, Sentence str2) {
    double[] vec1 = embeddings.get(mention.sentence.get(), mention.token_begin, mention.token_end);
    double[] vec2 = embeddings.get(str2);
    for(int i = 0; i < vec1.length; i++)
      features.incrementCount(prefix + "wv-" + i, vec1[i] - vec2[i]);
  }


  public void addBias(final Counter<String> features, final NumericMentionExpression e) {
    features.incrementCount("bias", 1);
  }

  public void addLengthBias(Counter<String> features, final NumericMentionExpression e) {
    features.incrementCount(e.expression.arguments().size() + "-narguments");
  }

  public void addFactFeatures(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    {
      for(NumericTuple tuple : args) {
//        String id1 = tuple.id.map(Object::toString).orElse(tuple.subj.replace("[^a-zA-Z0-9]","_"));
        String id1 = tuple.subj.replace("[^a-zA-Z0-9]","_");
        features.incrementCount("fact-" + id1);
      }
    }
  }

  public void addFactCrossFeatures(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    {
      for(NumericTuple tuple : args) {
//        String id1 = tuple.id.map(Object::toString).orElse(tuple.subj.replace("[^a-zA-Z0-9]","_"));
        String id1 = tuple.subj.replace("[^a-zA-Z0-9]","_");

        for(NumericTuple tuple_ : args) {
          if (tuple.equals(tuple_)) continue;
          // Make sure units are compatible.
//          if (!(tuple.unit.canMul(tuple_.unit) || tuple_.unit.canMul(tuple.unit))) continue;

//          String id2 = tuple_.id.map(Object::toString).orElse(tuple_.subj.replace("[^a-zA-Z0-9]","_"));
          String id2 = tuple_.subj.replace("[^a-zA-Z0-9]","_");

          if (id1.compareTo(id2) > 0)
            features.incrementCount("fact-" + id1 + "-" + id2);
          else
            features.incrementCount("fact-" + id2 + "-" + id1);
        }
      }
    }
  }

  public void addFactMentionFeatures(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    {
      for(NumericTuple tuple : args) {
        String id1 = tuple.subj.replace("[^a-zA-Z0-9]","_");
//        // Add non-stopword bigrams.
        List<String> words = e.mention_sentence.lemmas().stream()
            .filter(w -> !stopwords.contains(w)).collect(Collectors.toList());
        for(int i = 0; i < words.size(); i++) {
          String word = words.get(i);
          features.incrementCount("fact-" + id1 + "-" + word);
          if (i < words.size()-2) {
            String word_ = words.get(i+1);
            features.incrementCount("fact-" + id1 + "-" + word + "-" + word_);
          }
        }
      }
    }
  }

  public void addFactMentionWVFeatures(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    {
      for(NumericTuple tuple : args) {
        String id1 = tuple.subj.replace("[^a-zA-Z0-9]","_");
        semanticFeatures(features, "fact-" + id1 + "-", e.mention_sentence);
//        semanticFeatures(features, "fact-" + id1 + "-", e.mention);
//        // Add non-stopword bigrams.
//        features.incrementCount("fact-" + id1);
      }
    }
  }

  public void addNumericProximity(Counter<String> features, final NumericMentionExpression e) {
    double multiplier = e.mention_normalized_value / e.expression_value;
    features.incrementCount("numeric-distance", Math.log(multiplier));
    features.incrementCount("abs-numeric-distance", Math.abs(Math.log(multiplier)));
    features.incrementCount("sign-numeric-distance", Math.signum(Math.log(multiplier)));
  }

  public void addPerLengthNumericProximity(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    {
      double multiplier = e.mention_normalized_value / args.get(0).val;
      features.incrementCount("abs-numeric-distance-1", Math.abs(Math.log(multiplier)));
      features.incrementCount("sign-numeric-distance-1", Math.signum(Math.log(multiplier)));
    }
    if(args.size() > 1)
    {
      double multiplier = e.mention_normalized_value / args.get(1).val;
      features.incrementCount("abs-numeric-distance-2", Math.abs(Math.log(multiplier)));
      features.incrementCount("sign-numeric-distance-2", Math.signum(Math.log(multiplier)));
    }
    if(args.size() > 2)
    {
      double multiplier = e.mention_normalized_value / args.get(2).val;
      features.incrementCount("abs-numeric-distance-3", Math.abs(Math.log(multiplier)));
      features.incrementCount("sign-numeric-distance-3", Math.signum(Math.log(multiplier)));
    }
  }

  public void addCrossNumericProximity(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    if(args.size() > 1)
    {
      double multiplier = args.get(0).val / args.get(1).val;
      features.incrementCount("abs-numeric-distance-12", Math.abs(Math.log(multiplier)));
    }
    if(args.size() > 2)
    {
      double multiplier13 = args.get(0).val / args.get(2).val;
      double multiplier23 = args.get(1).val / args.get(2).val;
      features.incrementCount("abs-numeric-distance-13", Math.abs(Math.log(multiplier13)));
      features.incrementCount("abs-numeric-distance-23", Math.abs(Math.log(multiplier23)));
    }
  }

  public void addResponseSemanticSimilarity(Counter<String> features, final NumericMentionExpression e) {
    semanticSimilarityVW(features, "0-", e.mention, e.response);
  }

  public void addPerArgumentSemanticSimilarity(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    double sim = 0.;
    double minSim = 0.;
    double maxSim = 0.;
//    semanticSimilarityVW(features, "1-", e.mention, args.get(0).subjSentence);
    sim = semanticSimilarityVW(features, "1-", e.mention, args.get(0).subjSentence);
    maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
    if (args.size() > 1) {
      sim = semanticSimilarityVW(features, "2-", e.mention, args.get(1).subjSentence);
      maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
    }
//    semanticSimilarityVW(features, "2-", e.mention, args.get(1).subjSentence);
    if (args.size() > 2) {
      sim = semanticSimilarityVW(features, "3-", e.mention, args.get(2).subjSentence);
      maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
    }
//    semanticSimilarityVW(features, "3-", e.mention, args.get(2).subjSentence);
    features.incrementCount("max-semantic-similarity", maxSim);
    features.incrementCount("min-semantic-similarity", minSim);
  }

  public void addCrossArgumentSemanticSimilarity(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    double sim = 0.;
    double minSim = 0.;
    double maxSim = 0.;
    if(args.size() > 1)
    {
      sim = semanticSimilarityVW(features, "12-", args.get(0).subjSentence, args.get(1).subjSentence);
      maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
    }
    if(args.size() > 2)
    {
      sim = semanticSimilarityVW(features, "13-", args.get(0).subjSentence, args.get(2).subjSentence);
      maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
      sim = semanticSimilarityVW(features, "23-", args.get(1).subjSentence, args.get(2).subjSentence);
      maxSim = Math.max(maxSim, sim);  minSim = Math.min(minSim, sim);
    }
    features.incrementCount("max-cross-semantic-similarity", maxSim);
    features.incrementCount("min-cross-semantic-similarity", minSim);
  }

  public void addResponseVectors(Counter<String> features, final NumericMentionExpression e) {
    semanticCrossFeatures(features, "0-", e.mention_sentence, e.response);
  }

  public void addPerArgumentVectors(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    semanticCrossFeatures(features, "1-", e.mention, args.get(0).subjSentence);
    if (args.size() > 1)
      semanticCrossFeatures(features, "2-", e.mention, args.get(1).subjSentence);
    if (args.size() > 2)
      semanticCrossFeatures(features, "3-", e.mention, args.get(2).subjSentence);
  }

  public void addCrossArgumentVectors(Counter<String> features, final NumericMentionExpression e) {
    List<NumericTuple> args = e.expression.arguments();
    if(args.size() > 1)
    {
      semanticCrossFeatures(features, "12-", args.get(0).subjSentence, args.get(1).subjSentence);
    }
    if(args.size() > 2)
    {
      semanticCrossFeatures(features, "13-", args.get(0).subjSentence, args.get(2).subjSentence);
      semanticCrossFeatures(features, "23-", args.get(1).subjSentence, args.get(2).subjSentence);
    }
  }

  public static final List<String> templateOptions = Arrays.asList(
      "bias",
      "length-bias",
      "fact-id",
      "fact-cross",
      "fact-mention",
      "fact-mention-wv",
      "numeric-proximity",
//      "numeric-length-proximity",
//      "numeric-cross-proximity",
//      "response-ssimilarity",
      "arg-ssimilarity",
      "arg-cross-ssimilarity",
//      "response-wv",
      "arg-wv",
      "arg-cross-wv"
  );

  protected List<BiConsumer<Counter<String>, NumericMentionExpression>> getFeaturizers(String templates) {
    if(templates.equals("*"))
      templates = String.join(",", templateOptions);

    List<BiConsumer<Counter<String>, NumericMentionExpression>> fns = new ArrayList<>();
    for(String template : templates.split(",")) {
      template = template.trim();
      switch(template) {
        case "bias":
          fns.add(this::addBias);
          break;
        case "length-bias":
          fns.add(this::addLengthBias);
          break;
        case "fact-id":
          fns.add(this::addFactFeatures);
          break;
        case "fact-cross":
          fns.add(this::addFactCrossFeatures);
          break;
        case "fact-mention":
          fns.add(this::addFactMentionFeatures);
        case "fact-mention-wv":
          fns.add(this::addFactMentionWVFeatures);
          break;
        case "numeric-proximity":
          fns.add(this::addNumericProximity);
          break;
//        case "numeric-length-proximity":
//          fns.add(this::addPerLengthNumericProximity);
//          break;
//        case "numeric-cross-proximity":
//          fns.add(this::addCrossNumericProximity);
//          break;
        case "response-ssimilarity":
          fns.add(this::addResponseSemanticSimilarity);
          break;
        case "arg-ssimilarity":
          fns.add(this::addPerArgumentSemanticSimilarity);
          break;
        case "arg-cross-ssimilarity":
          fns.add(this::addCrossArgumentSemanticSimilarity);
          break;
        case "response-wv":
          fns.add(this::addResponseVectors);
          break;
        case "arg-wv":
          fns.add(this::addPerArgumentVectors);
          break;
        case "arg-cross-wv":
          fns.add(this::addCrossArgumentVectors);
          break;
        default:
          throw new IllegalArgumentException("Not a valid template: " + template + ". Choose from: " + String.join(", ", templateOptions));
      }
    }

    return fns;
  }

  public RVFDatum<Boolean, String> featurize(NumericMentionExpression e) {
    Counter<String> features = new ClassicCounter<>();
    featurizers.forEach(f -> f.accept(features, e));
    if (e.label.isPresent())
      return new RVFDatum<>(features, e.label.get() > 0.5);
    else
      return new RVFDatum<>(features);
  }

  public RVFDatum<Double, String> featurizeConfidence(NumericMentionExpression e) {
    Counter<String> features = new ClassicCounter<>();
    featurizers.forEach(f -> f.accept(features, e));
    if (e.label.isPresent())
      return new RVFDatum<>(features, e.label.get());
    else
      return new RVFDatum<>(features);
  }

}
