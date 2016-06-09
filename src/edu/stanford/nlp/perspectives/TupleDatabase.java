package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.graph.DirectedMultiGraph;
import edu.stanford.nlp.util.Triple;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.stanford.nlp.perspectives.NumericTuple.Unit;
import static edu.stanford.nlp.util.logging.Redwood.Util.*;

/**
 * Mock Database of tuples
 */
public class TupleDatabase extends DirectedMultiGraph<Unit, NumericTuple> {
  public final Map<Unit, List<NumericTuple>> tuples;
  public final UnitDatabase unitConversion;

  public TupleDatabase(UnitDatabase unitConversion) {
    this(new HashMap<>(), unitConversion);
  }

  public TupleDatabase(Map<Unit, List<NumericTuple>> tuples, UnitDatabase unitConversion) {
    super();
    this.tuples = tuples;
    this.unitConversion = unitConversion;
  }

  /**
   *
   * @param tuples_
   * @param unitConversions contains canonical units e.g. year -> time
   * @return
   */
  public static TupleDatabase compile(final List<NumericTuple> tuples_, UnitDatabase unitConversions) {
    startTrack("Constructing tuple database");
    final TupleDatabase db = new TupleDatabase(unitConversions); // Handle (per year)

    // Convert units of recieved tuples
    final List<NumericTuple> tuples = tuples_.stream()
        .map(unitConversions::standardizeUnits)
        .collect(Collectors.toList());
    // Add tuples to db.
    db.tuples.putAll(tuples.stream().collect(Collectors.groupingBy(t -> t.unit)));
    Stream.concat(tuples.stream().map(t -> t.unit),
        unitConversions.stream().flatMap(u -> Stream.of(u.originalUnit, u.finalUnit)))
    .forEach(db::addVertex);

    logf("Added %d tuples with %d units", tuples.size(), db.getNumVertices());

    // Add type conversions first.
    for (UnitTuple c : unitConversions) {
      // if the tuple has unit X per Y, then it can be used to convert a fact of unit Y to
      // Go through all the units and ask if this tuple can be multiplied to simplify the unit (i.e. it is contained in the unit).
      List<Unit> vertices_ = db.getAllVertices().stream().filter(u -> u.contains(c.originalUnit)).collect(Collectors.toList());
      vertices_.forEach(u -> db.add(u, u.convert(c), c));
    }
    logf("Expanded to %d units", db.getNumVertices());

    // Add edges between any two units if a tuple can convert between said units.
    for(NumericTuple tuple : tuples) {
      // if the tuple has unit X per Y, then it can be used to convert a fact of unit Y to
      // Go through all the units and ask if this tuple can be multiplied to simplify the unit (i.e. it is contained in the unit).
      List<Unit> vertices_ = db.getAllVertices().stream().filter(u -> u.contains(tuple.unit.invert())).collect(Collectors.toList());
      vertices_.forEach(u -> db.add(u, u.mul(tuple.unit), tuple));
    }
    logf("Expanded to %d units", db.getNumVertices());

    endTrack("Constructing tuple database");
    return db;
  }
  public static TupleDatabase compile(List<NumericTuple> tuples) {
    return compile(tuples, new UnitDatabase());
  }

  /**
   * This routine serializes a graph to a string in DOT format
   * @return
   */
  public String graphToDot() {
    StringBuilder sb = new StringBuilder();
    // Header
    sb.append("digraph {\n");
    for( Unit vertex : this.getAllVertices() ) {
      sb.append( String.format("%s [label = \"%s : %d\", penwidth=%f];\n", vertex.hashCode(), vertex.toString(), tuples.getOrDefault(vertex, Collections.emptyList()).size(), tuples.getOrDefault(vertex, Collections.emptyList()).size() * 0.5) );
    }
    for( Triple<Unit, Unit, List<NumericTuple>> triple : Util.groupedEdges(this) ) {
      sb.append( String.format( "%s -> %s [label=\"%d\"]; \n", triple.first.hashCode(), triple.second.hashCode(), triple.third.size()));
    }
    sb.append("}\n");

    return sb.toString();
  }

  public List<NumericTuple> getTuples() {
    List<NumericTuple> tuples = new ArrayList<>();
    this.tuples.values().forEach(tuples::addAll);
    return tuples;
  }

  /**
   * The total number of formulae that can be generated from the system.
   * @return
   */
  public long totalSize() {
    // For each node, follow all paths to final units.
    long count = 0;
    for(Unit u : this.getAllVertices()) {
      if (!u.isPrimary()) continue;
      count += (new GraphSearcher.GraphDFSSearcher(this)).dfs(new ExprBuilder(0., u)).count();
    }

    return count;
  }


}
