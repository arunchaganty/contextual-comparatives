
DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE (s.doc_char_end[array_length(s.doc_char_end,1)] - s.doc_char_begin[1] < 50) AND m.sentence_id = s.id;
DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE m.sentence_id = s.id AND gloss ~ E'US\\$ ?[0-9]+(,[0-9]+)?(\\.[0-9]+)?$';

DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE m.sentence_id = s.id AND gloss ~ E'--------';
