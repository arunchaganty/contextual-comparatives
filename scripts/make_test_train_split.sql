-- Get all numeric expressions that have only train data in their
-- arguments list
BEGIN;
-- DROP TABLE IF EXISTS cc_numericdata_test_ids CASCADE;
DROP TABLE IF EXISTS cc_numericdata_train_ids CASCADE;
DROP VIEW IF EXISTS cc_numericdata_test CASCADE;
DROP VIEW IF EXISTS cc_numericdata_train CASCADE;
CREATE TABLE cc_numericdata_train_ids (id INTEGER);
-- CREATE TABLE cc_numericdata_test_ids (id INTEGER);
-- \copy cc_numericdata_test_ids(id) FROM 'cc_numericdata_test_ids.tsv';
INSERT INTO cc_numericdata_train_ids (SELECT d.id FROM cc_numericdata AS d WHERE id NOT IN (SELECT id FROM cc_numericdata_test_ids));
CREATE VIEW cc_numericdata_test AS (SELECT e.* FROM cc_numericdata AS e, cc_numericdata_test_ids AS ids WHERE e.id = ids.id);
CREATE VIEW cc_numericdata_train AS (SELECT e.* FROM cc_numericdata AS e, cc_numericdata_train_ids AS ids WHERE e.id = ids.id);
--COMMIT;

--BEGIN;
DROP TABLE IF EXISTS cc_numericexpression_test_ids CASCADE;
DROP TABLE IF EXISTS cc_numericexpression_testonly_ids CASCADE;
DROP TABLE IF EXISTS cc_numericexpression_train_ids CASCADE;
DROP VIEW IF EXISTS cc_numericexpression_test CASCADE;
DROP VIEW IF EXISTS cc_numericexpression_train CASCADE;
CREATE TABLE cc_numericexpression_test_ids AS (SELECT DISTINCT ON (e.id) e.id AS id FROM cc_numericexpression AS e, cc_numericdata_test AS t WHERE t.id = ANY(e.arguments));
CREATE TABLE cc_numericexpression_train_ids AS (SELECT e.id FROM cc_numericexpression AS e WHERE e.id NOT IN (SELECT id FROM cc_numericexpression_test_ids));
CREATE TABLE cc_numericexpression_testonly_ids AS (SELECT e.id FROM cc_numericexpression e WHERE e.id NOT IN (SELECT DISTINCT ON (e.id) e.id AS id FROM cc_numericexpression AS e, cc_numericdata_train AS t WHERE t.id = ANY(e.arguments)));
CREATE VIEW cc_numericexpression_train AS (SELECT e.* FROM cc_numericexpression AS e, cc_numericexpression_train_ids AS ids WHERE e.id = ids.id);
CREATE VIEW cc_numericexpression_test AS (SELECT e.* FROM cc_numericexpression AS e, cc_numericexpression_test_ids AS ids WHERE e.id = ids.id);
CREATE VIEW cc_numericexpression_testonly AS (SELECT e.* FROM cc_numericexpression AS e, cc_numericexpression_testonly_ids AS ids WHERE e.id = ids.id);
--COMMIT;

--BEGIN;
DROP TABLE IF EXISTS cc_numericexpressionresponse_test_ids CASCADE;
DROP TABLE IF EXISTS cc_numericexpressionresponse_testonly_ids CASCADE;
DROP TABLE IF EXISTS cc_numericexpressionresponse_train_ids CASCADE;
DROP VIEW IF EXISTS cc_numericexpressionresponse_test CASCADE;
DROP VIEW IF EXISTS cc_numericexpressionresponse_testonly CASCADE;
DROP VIEW IF EXISTS cc_numericexpressionresponse_train CASCADE;
-- Get all responses for train and test expressions.
CREATE TABLE cc_numericexpressionresponse_train_ids AS (SELECT r.id FROM cc_numericexpressionresponse AS r, cc_numericexpression_train_ids AS ids WHERE r.expression_id = ids.id);
CREATE TABLE cc_numericexpressionresponse_test_ids AS (SELECT r.id FROM cc_numericexpressionresponse AS r, cc_numericexpression_test_ids AS ids WHERE r.expression_id = ids.id);
CREATE TABLE cc_numericexpressionresponse_testonly_ids AS (SELECT r.id FROM cc_numericexpressionresponse AS r, cc_numericexpression_testonly_ids AS ids WHERE r.expression_id = ids.id);
CREATE VIEW cc_numericexpressionresponse_train AS (SELECT r.* FROM cc_numericexpressionresponse AS r, cc_numericexpressionresponse_train_ids AS ids WHERE r.id = ids.id);
CREATE VIEW cc_numericexpressionresponse_test AS (SELECT r.* FROM cc_numericexpressionresponse AS r, cc_numericexpressionresponse_test_ids AS ids WHERE r.id = ids.id);
CREATE VIEW cc_numericexpressionresponse_testonly AS (SELECT r.* FROM cc_numericexpressionresponse AS r, cc_numericexpressionresponse_testonly_ids AS ids WHERE r.id = ids.id);
COMMIT;
