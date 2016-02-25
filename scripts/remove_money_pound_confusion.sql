-- Guard against mentions that look like "500 pounds (US$ 100)" which
-- show up fairly often.
DELETE FROM cc_numericmention m USING cc_numericmention n WHERE m.sentence_id = n.sentence_id AND  m.unit='pound' AND n.normalized_unit='money';
