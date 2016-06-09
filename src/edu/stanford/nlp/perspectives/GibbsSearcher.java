package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util.Either;
import edu.stanford.nlp.arguments.Util.WordEmbedding;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.nlp.arguments.Util.choose;
import static edu.stanford.nlp.arguments.Util.normalize;
import static edu.stanford.nlp.arguments.Util.sample;
import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * Randomly generate comparatives.
 */
public class GibbsSearcher implements Searcher {
  @ArgumentParser.Option(gloss="Random seed")
  public static int seed = 42;

  @ArgumentParser.Option(gloss="Weight for closeness")
  public final double closeness = 1.;

  @ArgumentParser.Option(gloss="Weight for semantic similarity")
  public final double similarity = 1.;

  private final Random rng = new Random(seed);
  protected final TupleDatabase tdb;
  protected final WordEmbedding embedding;
  protected final List<UnitTuple> unitTuples;

  public GibbsSearcher(TupleDatabase tdb) {
    this.tdb = tdb;
    this.embedding = new WordEmbedding(0, Collections.emptyMap());
    this.unitTuples = Collections.emptyList();
  }
  public GibbsSearcher(TupleDatabase tdb, WordEmbedding embedding) {
    this.tdb = tdb;
    this.embedding = embedding;
    this.unitTuples = Collections.emptyList();
  }
  public GibbsSearcher(TupleDatabase tdb, WordEmbedding embedding, List<UnitTuple> unitTuples) {
    this.tdb = tdb;
    this.embedding = embedding;
    this.unitTuples = unitTuples;
  }

  public TupleDatabase getTdb() {return tdb;}

  public static class SearchFailureException extends Exception {
    public SearchFailureException(String message) {super(message);}
  }

  /**
   * Sample a tuple by filtering by unit, sorting by value + contextual similarity.
   * @param builder -- gives us the state to allow us to make wise decisions.
   * @return
   */
  public NumericTuple getTuple(final Optional<NumericMention> mention, final ExprBuilder builder) throws SearchFailureException {

    // TODO: convert to stream based thing for efficiency?
    // filter by unit.

    final List<NumericTuple> tuples =
        (builder.expectedUnit.isPresent()) ?
            tdb.getTuples().stream().filter(t -> t.unit.equals(builder.expectedUnit.get())).collect(Collectors.toList()) :
            tdb.getTuples();
    if(tuples.size() == 0)
      throw new SearchFailureException("Search failed at: " + builder.parentest().toString());

    final double[] mentionVector = embedding.get(mention.map(m -> m.sentence.get().toString()).orElse(""));
    // Sort by closeness to expected value (if present).
    // comparison objective.
//    int salt = rng.nextInt(); // for randomness.
    Map<NumericTuple,Double> candidates = tuples.stream().map(t -> {
      double valueScore = builder.expectedValue.map(v -> t.val / v).orElse(1.); // Will be an absolute number. Smaller is better.
      valueScore = Math.abs(Math.log(valueScore)) / Math.log(10.); // Badness is on a log-scale. I want 1 => good. and within a factor of 10 is still considered to be positive.
      double semanticScore = WordEmbedding.dotProduct(mentionVector, embedding.get(t.subj + " " + t.pred)); // lies between -1, 1; is a cosine. 1 is better.
      double score = closeness * valueScore + similarity * (1-semanticScore); // Making semanticity in [0,2]
      score = Math.exp(-score); // make this favor smaller values.
      return Pair.makePair(score, t);
    }).sorted((p1, p2) -> Double.compare(p2.first, p1.first)) // Sort in decreasing order.
        .limit(10)
        .collect(Collectors.toMap(Pair::second, Pair::first));

    final Counter<NumericTuple> dist = new ClassicCounter<>(candidates);
    normalize(dist);
    return sample( rng, dist ); //candidates.get(0).second;
  }

  public Expr generate(final Optional<NumericMention> mention, ExprBuilder builder) {
    for(int tries = 0; tries < 10; tries++) {
      try {
        startTrack("generate");
        final ExprBuilder builder_ = new ExprBuilder(builder); // Keep a copy to handle retries.
        Either<SearchState, Expr> next = Either.left(new SearchState(builder_));
        while (next.left.isPresent()) {
          SearchState state = next.left.get();

          Counter<Expr.Operation> operationDist =
              new ClassicCounter<>(state.nextActions().stream().collect(Collectors
                  .toMap(v -> v, v -> v.isTuple() ? 1. : 1./(builder_.depth+1))));
          normalize(operationDist);
          Expr.Operation op = sample( rng, operationDist );
          if (op.isTuple()) {
            next = state.apply(getTuple(mention, state.builder));
          } else if (op.isUnit()) {
            next = state.apply(getUnitTuple(state.builder));
          }
          else {
            next = state.apply(op);
          }
        }
        assert next.right.isPresent();
        endTrack("generate");

        return next.right.get();
      } catch (SearchFailureException e) {
        endTrack("generate");
//        warn(e.getMessage());
      }
    }
    throw new RuntimeException("Couldn't generate a sample");
  }

  // Sample a unit tuple.
  public UnitTuple getUnitTuple(ExprBuilder builder) throws SearchFailureException {
    List<UnitTuple> list;
    if(builder.expectedUnit.isPresent()) {
      list = unitTuples.stream().filter(t -> builder.expectedUnit.get().contains(t.finalUnit)).collect(Collectors.toList());
    } else {
      list = unitTuples;
    }
    if (list.size() == 0)
      throw new SearchFailureException("Couldn't convert to unit");
    return choose(rng, list);
  }

  public List<Expr> generate(final NumericMention mention) {
    return Collections.singletonList(generate(Optional.of(mention), new ExprBuilder(mention.value, mention.unit)));
  }

  /**
   * Stores the search state.
   */
  public static class SearchState {
    private static final int MAX_DEPTH = 2;

    // The expression that is being acted on.
    public final ExprBuilder builder;
    // Target values and units are accessible through the expr builder
  //  public final double targetValue;
  //  public final NumericTuple.Unit targetUnit;

    public SearchState(ExprBuilder builder) {
      this.builder = builder;
    }

    /**
     * Returns the set of possible next actions. Tuple encodes several choices.
     * @return
     */
    public List<Expr.Operation> nextActions() {
      assert !builder.op.isPresent(); // if the op was set, we'd be done.

      if (builder.depth < MAX_DEPTH) {
  //      return Arrays.asList(Operation.values());
        return Arrays.asList(Expr.Operation.MUL, Expr.Operation.UNIT, Expr.Operation.TUPLE);
  //      return Arrays.asList(Operation.UNIT, Operation.TUPLE);
      } else {
        return Collections.singletonList(Expr.Operation.TUPLE);
      }
    }

    /**
     * Applies operation to builder and returns a new state.
     * Will throw assertion if op == TUPLE
     * @param op
     * @return
     */
    public Either<SearchState,Expr> apply(Expr.Operation op) {
      assert op.isOp(); // Call the other function for tuples.
      return Either.left(new SearchState(builder.op(op)));
    }
    /**
     * Applies TUPLE operation to partial, returns a new state.
     * @param tuple
     * @return
     */
    public Either<SearchState,Expr> apply(NumericTuple tuple) {
      return builder.tuple(tuple).mapLeft(SearchState::new);
    }

    public Either<SearchState,Expr> apply(UnitTuple tuple) {
      return Either.left(new SearchState(builder.unitChange(tuple)));
    }

  }
}
