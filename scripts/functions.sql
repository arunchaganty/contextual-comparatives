CREATE OR REPLACE FUNCTION range_contains(numeric, numeric, numeric) RETURNS boolean AS
    $$
    SELECT $1 <= $3 AND $3 < $2;
    $$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION range_contains(double precision, double precision, double precision) RETURNS boolean AS
    $$
    SELECT $1 <= $3 AND $3 < $2;
    $$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION range_contains(integer, integer, integer) RETURNS boolean AS
    $$
    SELECT $1 <= $3 AND $3 < $2;
    $$ LANGUAGE SQL;
