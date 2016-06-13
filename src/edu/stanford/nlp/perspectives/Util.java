package edu.stanford.nlp.perspectives;

import edu.stanford.nlp.graph.DirectedMultiGraph;
import edu.stanford.nlp.io.RecordIterator;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.stats.ClassicCounter;
import edu.stanford.nlp.stats.Counter;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.TSVSentenceIterator;
import edu.stanford.nlp.util.Triple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static edu.stanford.nlp.perspectives.NumericTuple.Unit;
import static edu.stanford.nlp.util.TSVSentenceIterator.SentenceField;
import static edu.stanford.nlp.util.TSVSentenceIterator.toSentence;
import static edu.stanford.nlp.util.logging.Redwood.Util.log;

/**
 * Various utility routines.
 */
public class Util {
  public final static class Either<L,R>
  {
    public static <L,R> Either<L,R> left(L value) {
      return new Either<>(Optional.of(value), Optional.empty());
    }
    public static <L,R> Either<L,R> right(R value) {
      return new Either<>(Optional.empty(), Optional.of(value));
    }
    public final Optional<L> left;
    public final Optional<R> right;
    private Either(Optional<L> l, Optional<R> r) {
      left=l;
      right=r;
    }
    public <T> T map(
        Function<? super L, ? extends T> lFunc,
        Function<? super R, ? extends T> rFunc)
    {
      return left.map(lFunc).orElseGet(()->right.map(rFunc).get());
    }
    public <T> Either<T,R> mapLeft(Function<? super L, ? extends T> lFunc)
    {
      return new Either<>(left.map(lFunc),right);
    }
    public <T> Either<L,T> mapRight(Function<? super R, ? extends T> rFunc)
    {
      return new Either<>(left, right.map(rFunc));
    }
    public void apply(Consumer<? super L> lFunc, Consumer<? super R> rFunc)
    {
      left.ifPresent(lFunc);
      right.ifPresent(rFunc);
    }
  }

  public static <T> T choose(Random rng, List<T> elem) {
    return elem.get(rng.nextInt(elem.size()));
  }

  public static class WordEmbedding {
    private final Map<String, double[]> embedding;
    public final int dim;
    public final boolean isCaseAware = false;

    private final HashSet<String> stopWords = new StopWords();

    public WordEmbedding(int dim, Map<String, double[]> embedding) {
      this.dim = dim;
      this.embedding = embedding;
    }
    public static WordEmbedding empty() {return new WordEmbedding(0, Collections.emptyMap());}
    public static WordEmbedding loadFromFile(BufferedReader input) throws IOException {
      log("Reading word embeddings...");
      String line = input.readLine();
      Optional<Integer> dim = Optional.empty();

      Map<String, double[]> embedding = new HashMap<>();
      while (line != null) {
        line = line.trim(); // Get rid of newlines
        // Line has format "word\tvecdim"
        String[] parts = line.split("\t");
        String name = parts[0];
        parts = parts[1].split(" ");

        // Check sizes
        if(!dim.isPresent()) {
          dim = Optional.of(parts.length);
        }
        else{
          assert dim.get() == parts.length;
        }

        // Copy values to array
        double[] values = new double[parts.length];
        for(int i = 0; i < parts.length; i++) {
          values[i] = Double.parseDouble(parts[i]);
        }
        embedding.put(name, values);

        line = input.readLine();
      }
      assert dim.isPresent();
      log("Done.");
      return new WordEmbedding(dim.get(), embedding);
    }
    public static WordEmbedding loadFromFile(String filename) {
      try {
        return loadFromFile(new BufferedReader(new FileReader(filename)));
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
    }
    public static double dotProduct(double[] v1, double[] v2) {
      assert v1.length == v2.length;
      double ret = 0.;
      double v1norm = 0.;
      double v2norm = 0.;
      for(int i = 0; i < v1.length; i++) {
        v1norm += v1[i] * v1[i];
        v2norm += v2[i] * v2[i];
        ret += v1[i] * v2[i];
      }
      if (v1norm * v2norm > 1e-10) { // If this isn't true, then well, ret is ~= 0 anyways.
        return ret / Math.sqrt(v1norm * v2norm);
      } else {
        return ret;
      }
    }

    public static double[] sum(double[] v1, double[] v2) {
      assert v1.length == v2.length;
      double[] ret = new double[v1.length];
      for(int i = 0; i < v1.length; i++) {
        ret[i] += v1[i] + v2[i];
      }
      return ret;
    }

    /**
     * Place results into v1
     * @param v1
     * @param v2
     */
    public static double[] sumEq(double[] v1, double[] v2) {
      assert v1.length == v2.length;
      for(int i = 0; i < v1.length; i++) {
        v1[i] += v2[i];
      }
      return v1;
    }

    public static double[] maxEq(double[] v1, double[] v2) {
      assert v1.length == v2.length;
      for(int i = 0; i < v1.length; i++) {
        v1[i] = Math.max(v1[i], v2[i]);
      }
      return v1;
    }

    public static double[] scaleEq(double[] v1, double m) {
      for(int i = 0; i < v1.length; i++)
        v1[i] *= m;
      return v1;
    }

    public static double[] scale(double[] v1, double m) {
      double[] ret = new double[v1.length];
      System.arraycopy(v1, 0, ret, 0, v1.length);
      for(int i = 0; i < v1.length; i++)
        ret[i] *= m;
      return ret;
    }

    public double[] getW(String seq) {
      if (!isCaseAware) seq = seq.toLowerCase();
      if (embedding.containsKey(seq)) {
        return embedding.get(seq);
      } else if (embedding.containsKey(seq.toLowerCase())) {
        return embedding.get(seq.toLowerCase());
      } else {
        return new double[dim]; // WARNING: this is really a random word right now?!
      }
    }

    public int size() {return embedding.size();}

    /**
     * Get a phrase word-vector
     *    - remove stopwords.
     * @param seq
     * @return
     */
    public double[] get(String seq) {
      return get(seq, 0, Integer.MAX_VALUE);
    }

    public double[] get(Sentence seq) {
      return get(seq, 0, Integer.MAX_VALUE);
    }

    /**
     * Get a vector focussed at [start,end] and attenuating through the sentence.
     * @param seq
     * @param start
     * @param end
     * @return
     */
    public double[] get(String seq, int start, int end) {
      return get(Arrays.asList(seq.split("\\s+")), start, end);
    }


    public double[] get(Sentence seq, int start, int end) {
      return get(seq.words(), start, end);
    }

    public double[] get(List<String> words, int start, int end) {

      double[] ret = new double[dim];
      int count = 0;
      for(int i = 0; i < words.size(); i++) {
        String w = words.get(i);
        if (!isCaseAware) w = w.toLowerCase();
        if (stopWords.contains(w)) continue; // ignore stopwords.
        // Scale by the distance from start / end.
        double scale = 1.0;
        if (i < start)
          scale = Math.exp(i - start);
        else if (i > end)
          scale = Math.exp(end - i);
        assert scale <= 1.0;

        double[] v = scale(getW(w), scale);
        if(i == 0)
          System.arraycopy(v,0,ret,0,v.length);
        else
          sumEq(ret, v); // average
        count++;
      }
      if (count > 0)
        return scaleEq(ret, 1./count);
      else
        return ret;
    }

  }


  public static <E> double sum(Counter<E> counter) {
    return counter.entrySet().stream().collect(Collectors.summingDouble(Map.Entry::getValue));
  }
  public static <E> void normalize(Counter<E> counter) {
    double sum = sum(counter);
    counter.entrySet().forEach(e -> e.setValue(e.getValue()/sum));
  }

  public static <E> E sample(Random rng, Counter<E> counter) {
    assert Math.abs(sum(counter) - 1.0) < 1e-8;
    double s = 0.;
    double val = rng.nextDouble();
    for(Map.Entry<E, Double> e : counter.entrySet()) {
      s += e.getValue();
      if (val < s) return e.getKey();
    }
    throw new RuntimeException("Improper counter");
  }

  public static List<NumericTuple> readNumericData(String filename) {
    List<NumericTuple> data = new ArrayList<>();
    RecordIterator rit;
    try {
      rit = new RecordIterator(filename, "\t");
    } catch (FileNotFoundException e) {
      throw new RuntimeIOException(e);
    }
    while(rit.hasNext()) {
      List<String> entries = rit.next();
      NumericTuple tuple = new NumericTuple(
          Integer.parseInt(entries.get(0)),    //id
          entries.get(1),                     // name
          entries.get(2),                     // relation
          Double.parseDouble(entries.get(3)), // value
          entries.get(4)                     // unit
      );
      data.add(tuple);
    }
    return data;
  }

  public static final List<TSVSentenceIterator.SentenceField> fields = new ArrayList<SentenceField>() {{
    add(SentenceField.ID);                            //    id             | integer   | not null default nextval('javanlp_sentence_id_seq'::regclass)
    add(SentenceField.DOC_ID);                        //    doc_id         | text      | not null
    add(SentenceField.IGNORE);                        //    sentence_index | integer   | not null
    add(SentenceField.GLOSS);                         //        gloss          | text      | not null
    add(SentenceField.WORDS);                         //    words          | text[]    | not null
    add(SentenceField.LEMMAS);                        //    lemmas         | text[]    | not null
    add(SentenceField.POS_TAGS);                      //    pos_tags       | text[]    | not null
    add(SentenceField.NER_TAGS);                      //    ner_tags       | text[]    | not null
    add(SentenceField.DOC_CHAR_BEGIN);                //    doc_char_begin | integer[] | not null
    add(SentenceField.DOC_CHAR_END);                  //    doc_char_end   | integer[] | not null
    add(SentenceField.IGNORE);                        //    dependencies   | text      |
  }};


  public static List<NumericMention> readMentions(String filename) throws FileNotFoundException {
    List<NumericMention> mentions = new ArrayList<>();
    RecordIterator rit = new RecordIterator(filename, 10 + fields.size(), true, "\t");

    List<String> header = Arrays.asList("id	value	unit	normalized_value	normalized_unit	sentence_id	doc_char_begin	doc_char_end	token_begin	token_end	id	doc_id	sentence_index	gloss	words	lemmas	pos_tags	ner_tags	doc_char_begin	doc_char_end	dependencies".split("\\s+"));
    List<String> header_ = rit.next();
    assert header.equals(header_);

    // Skip header.
//    assert rit.hasNext(); rit.next();
    while(rit.hasNext()) {
      List<String> entries = rit.next();
      List<String> numericMentionEntries = entries.subList(0, 10);
      List<String> sentenceEntries = entries.subList(10, entries.size());
      Sentence sentence = toSentence(fields, sentenceEntries);

      // m.id, m.value, m.unit, m.normalized_value, m.normalized_unit, m.sentence_id, m.doc_char_begin, m.doc_char_end, m.token_begin, m.token_end
      NumericMention mention = new NumericMention(
          Integer.parseInt(numericMentionEntries.get(0)),    //id
          Double.parseDouble(numericMentionEntries.get(1)), // value
          Unit.of(numericMentionEntries.get(2)),                     // unit
          Double.parseDouble(numericMentionEntries.get(3)), // value
          Unit.of(numericMentionEntries.get(4)),                     // unit
          numericMentionEntries.get(5),                     // sentence_id
          Integer.parseInt(numericMentionEntries.get(6)),  // doc_char_begin
          Integer.parseInt(numericMentionEntries.get(7)),   // doc_char_end
          Integer.parseInt(numericMentionEntries.get(8)),   // token_begin
          Integer.parseInt(numericMentionEntries.get(9)),   // token_end
          sentence
      );
      mentions.add(mention);
    }
    return mentions;
  }

  public static <T1,T2> List<Triple<T1,T1,List<T2>>> groupedEdges(DirectedMultiGraph<T1,T2> graph ) {
    List<Triple<T1,T1,List<T2>>> edges = new ArrayList<>();
    for( T1 head : graph.getAllVertices() ) {
      for( T1 tail : graph.getChildren(head) ) {
        edges.add( Triple.makeTriple(head, tail, graph.getEdges(head, tail)) );
      }
    }
    return edges;
  }

  /**
   * Write an array of objects to a Postgres array, taking each object's toString() method.
   * @param inputArray The array to join.
   * @param <E> The type of the objects to join.
   * @return A string representation of the joined array, properly escaped.
   */
  public static <E> String writeArray(List<E> inputArray) {
    return "{" + StringUtils.join(inputArray.stream().map(x -> {
      String gloss = x.toString();
      return "\"" +
          gloss.replace("\\", "\\\\")
              .replace("\"", "\\\"") +
          "\"";
    }).collect(Collectors.toList()), ",") + "}";
  }

  /**
   * Convert an existing map to a Counter, perserving elements.
   * The Counter is backed by a HashMap.
   *
   * @param collection Each item in the Collection is made a key in the
   *     Counter with count being its multiplicity in the Collection.
   */
  public static <E> ClassicCounter createClassicCounter(Map<E,Double> collection) {
    ClassicCounter<E> c = new ClassicCounter<>();
    for (Map.Entry<E, Double> entry : collection.entrySet()) {
      c.incrementCount(entry.getKey(), entry.getValue());
    }
    return c;
  }
}
