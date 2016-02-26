
-- DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE length(s.gloss) < 50 AND m.sentence_id = s.id;
DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE s.doc_id ~ '^((bolt-)|(eng-))' AND m.sentence_id = s.id;
-- DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE m.sentence_id = s.id AND gloss ~ E'US\\$ ?[0-9]+(,[0-9]+)?(\\.[0-9]+)?$';
-- DELETE FROM cc_numericmention m USING javanlp_sentence s WHERE m.sentence_id = s.id AND gloss ~ E'--------';
