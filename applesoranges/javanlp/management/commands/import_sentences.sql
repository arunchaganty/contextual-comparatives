COPY javanlp_sentence(id, doc_id, sentence_index, gloss, words, lemmas, pos_tags, ner_tags, doc_char_begin, doc_char_end, dependencies) 
FROM '/home/chaganty/Research/contextual-comparatives/examples/money.tsv' WITH DELIMITER E'\t';
