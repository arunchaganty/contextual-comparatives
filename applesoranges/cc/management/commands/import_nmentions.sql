COPY cc_numericmention(value, unit, type, sentence_id, doc_char_begin, doc_char_end, token_begin, token_end)
FROM '/home/chaganty/Research/contextual-comparatives/data/money_mentions.tsv' WITH CSV HEADER DELIMITER E'\t';
