DELETE FROM cc_numericexpression;
COPY cc_numericexpression(multiplier,arguments,value,unit) FROM '/Users/chaganty/Research/contextual-comparatives/data/comparisons/comparisons_5000.tsv' WITH CSV DELIMITER E'\t';
