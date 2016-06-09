package edu.stanford.nlp.perspectives;

import java.util.List;

/**
 * Created by chaganty on 2/9/16.
 */
public interface Searcher {
  List<Expr> generate(NumericMention mention);
  TupleDatabase getTdb();
}
