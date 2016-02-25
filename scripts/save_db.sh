#!/bin/bash

set -o xtrace

echo "Backing up db"
pg_dump cc > data/cc.sql;
rsync data/cc.sql jamie.stanford.edu:dump/cc.sql 
echo "done!"

