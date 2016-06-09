package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.arguments.Util;
import edu.stanford.nlp.arguments.Util.*;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.ArgumentParser;
import edu.stanford.nlp.util.Pair;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.nlp.arguments.Util.*;
import static edu.stanford.nlp.perspectives.NumericTuple.*;
import static edu.stanford.nlp.util.logging.Redwood.Util.endTrack;
import static edu.stanford.nlp.util.logging.Redwood.Util.startTrack;

/**
 * Randomly generate comparatives.
 */
public class GraphSearcher implements Searcher {
  @ArgumentParser.Option(gloss="Random seed")
  public static int seed = 42;

  @ArgumentParser.Option(gloss="Weight for closeness")
  public final double closeness = 1.;

  @ArgumentParser.Option(gloss="Weight for semantic similarity")
  public final double similarity = 1.;

  private final Random rng = new Random(seed);
  protected final TupleDatabase tdb;
  protected final WordEmbedding embedding;

  public GraphSearcher(TupleDatabase tdb) {
    this.tdb = tdb;
    this.embedding = new WordEmbedding(0, Collections.emptyMap());
  }
  public GraphSearcher(TupleDatabase tdb, WordEmbedding embedding) {
    this.tdb = tdb;
    this.embedding = embedding;
  }

  public TupleDatabase getTdb() {return tdb;}

  protected static class GraphDFSSearcher {
    // TODO(chaganty): Note that right now, visited lists don't matter because we are only traversing edges in a single direction.
//    protected final List<Unit> visited;

    final TupleDatabase graph;

    public GraphDFSSearcher(TupleDatabase graph) {
      this.graph = graph;
//      this.visited = new ArrayList<>();
    }

    public Stream<Expr> dfs(final ExprBuilder builder) {
      Unit target = builder.expectedUnit.get();
//      assert !visited.contains(target);

      // add target to the visited list.
//      visited.add(target);

      // For every tuple at this node, return an expression.
      return
          Stream.concat(
          graph.tuples.getOrDefault(target, Collections.emptyList()).stream()
                  .map(t -> new ExprBuilder(builder).tuple(t).right.get()), // Construction guarantees that adding tuples here always completes construction.
      // For every outedge from this node, call recursively. (note this is criminal in terms of memory!)
        graph.getIncomingEdges(target).stream()
            .map(t -> {
              if (t instanceof UnitTuple) {
                return new ExprBuilder(builder).unitChange((UnitTuple) t);
              } else {
                return new ExprBuilder(builder).mul().tuple(t).left.get();
              }})
//            .filter(eb -> !visited.contains(eb.expectedUnit.get()))
            .flatMap(this::dfs));
      // TODO(chaganty): support division -- here I should be allowed to take two units and divide them "cars / number of people = car per person"
    }
  }

  public List<Pair<Expr,Double>> reorder(NumericMention mention, List<Expr> exprs) {
    final double[] mentionVector = embedding.get(mention.sentence.map(Object::toString).orElse(""));
    return exprs.stream().flatMap(e -> {
      NumericTuple t = e.evaluate();
      double valueScore = Math.abs((Math.log(mention.value) - Math.log(t.val)) / Math.log(10)); // Relative score. Smaller is better.
      double semanticScore = Util.WordEmbedding.dotProduct(mentionVector, embedding.get(t.subj + " " + t.pred)); // lies between -1, 1; is a cosine. 1 is better.
      double score = closeness * valueScore + similarity * (1 - semanticScore); // Making semanticity in [0,2]
      score = Math.exp(-score); // make this favor smaller values.

      if(valueScore > 2.) // If it's greater than two orders of magnitude apart (100 to 0.01 is ok).
        return Stream.empty();
      else
        return Stream.of(Pair.makePair(e, score));
    })
        .sorted((p1, p2) -> Double.compare(p2.second, p1.second)) // Sort in decreasing order.
        .collect(Collectors.toList());
  }

  /**
   * Enumerate expression trees given a numeric mention unit.
   * @param mention
   * @return
   */
  public List<Expr> generate(NumericMention mention) {
    // -- for a later time
    // 1. clone the database.
    // 2. add unit + all conversions.
    // --
    final TupleDatabase db = this.tdb;

    // 3. Execute DFS from the mention's unit.
    return new GraphDFSSearcher(db).dfs(new ExprBuilder(mention.value, mention.unit))
    .collect(Collectors.toList());
  }
}
