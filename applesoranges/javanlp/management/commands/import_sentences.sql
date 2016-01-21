COPY javanlp_sentence(doc_id, sentence_index, words, lemmas, pos_tags, ner_tags, doc_char_begin, doc_char_end, dependencies, gloss, constituencies) 
FROM STDIN WITH CSV DELIMITER E'\t';

