-- Get all expressions 
DELETE FROM cc_numericmentionexpression;
INSERT INTO cc_numericmentionexpression (SELECT nextval('cc_numericmentionexpression_id_seq'::regclass) as id, (m.normalized_value / e.value) AS multiplier, e.id as expression_id, m.id as mention_id FROM cc_numericmention_train AS m, cc_numericexpression_train AS e WHERE m.normalized_unit = e.unit AND range_contains(0.1, 11, e.multiplier)  AND range_contains(0.5 * e.multiplier, 2 * e.multiplier, (m.normalized_value / e.value))  AND e.value > 0);
