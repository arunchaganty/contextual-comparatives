COPY cc_numericdata(name,relation,value,unit,type,qualifiers)
FROM '/home/chaganty/Research/contextual-comparatives/data/money_data.tsv' WITH CSV HEADER DELIMITER E'\t';
