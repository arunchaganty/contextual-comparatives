-- Create a dataset of mentions.
-- For each of the following units.

CREATE OR REPLACE FUNCTION range_contains(numeric, numeric, numeric) RETURNS boolean AS
    $$
    SELECT $1 <= $3 AND $3 < $2;
    $$ LANGUAGE SQL;

-- Choose mention ids where the element is within some range, and choose
-- 200 such instances (to be split).
-- person
DROP VIEW IF EXISTS cc_numericmention_train;
DROP TABLE IF EXISTS cc_numericmention_train_ids;
CREATE TABLE cc_numericmention_train_ids(id integer);

--  time  
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 40);       -- 59
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);       -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 100);       -- 142
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 30);        -- 41
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 2);        -- 3
--  area                                                                                                                                                                                
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 15);       -- 20
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);       -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);       -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);       -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 7);        -- 10
--  weight                                                                                                                                                                              
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 150);    -- 199
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 200);    -- 200
--  money                                                                                                                                                                               
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 40);      -- 57
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);      -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);      -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);      -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 200);     -- 200
--  volume                                                                                                                                                                              
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 10);     -- 15
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 200);    -- 200
--  length                                                                                                                                                                              
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e-3, 1e0, value::numeric) ORDER BY random() LIMIT 70);     -- 115
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e0, 1e3, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 20);      -- 34
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 5);      -- 8
--  car                                                                                                                                                                                 
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e0, 1e2, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e2, 1e3, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 4);         -- 6
-- person                                                                                                                                                                               
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e0, 1e2, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e2, 1e3, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 200);     -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 60);     -- 90
--  gun                                                                                                                                                                                 
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e0, 1e2, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e2, 1e3, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e3, 1e6, value::numeric) ORDER BY random() LIMIT 200);        -- 200
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e6, 1e9, value::numeric) ORDER BY random() LIMIT 100);        -- 134
INSERT INTO cc_numericmention_train_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e9, 1e12, value::numeric) ORDER BY random() LIMIT 1);         -- 1

CREATE VIEW cc_numericmention_train AS (SELECT m.* FROM cc_numericmention m, cc_numericmention_train_ids ids WHERE m.id = ids.id);

DROP VIEW IF EXISTS cc_numericmention_test;
DROP TABLE IF EXISTS cc_numericmention_test_ids;
CREATE TABLE cc_numericmention_test_ids(id integer);
--  time  
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='time' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  area  
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='area' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  weight
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='weight' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  money 
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='money' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  volume
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='volume' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  length
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e-3, 1e0, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e0, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='length' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  car   
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e0, 1e2, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e2, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='car' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
-- person
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e0, 1e2, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e2, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='person' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
--  gun   
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e0, 1e2, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e2, 1e3, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e3, 1e6, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e6, 1e9, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);
INSERT INTO cc_numericmention_test_ids (SELECT id FROM cc_numericmention WHERE normalized_unit='gun' AND range_contains(1e9, 1e12, value::numeric)AND id NOT IN (SELECT id FROM cc_numericmention_train_ids) ORDER BY random() LIMIT 100);

CREATE VIEW cc_numericmention_test AS (SELECT m.* FROM cc_numericmention m, cc_numericmention_test_ids ids WHERE m.id = ids.id);

