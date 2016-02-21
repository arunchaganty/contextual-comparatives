DELETE FROM cc_numericdata;
COPY cc_numericdata(name,relation,value,unit) FROM '/home/chaganty/Research/contextual-comparatives/data/numeric_data.tsv' WITH CSV DELIMITER E'\t';
