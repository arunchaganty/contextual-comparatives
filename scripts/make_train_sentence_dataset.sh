#!/bin/bash
#
set -o nounset
set -o errexit

# Load the prerequisite environment variables
TASK_FILE="${BASH_SOURCE[0]}"
if [[ "$TASK_FILE" != /* ]]; then TASK_FILE="`pwd`/$TASK_FILE"; fi
MYDIR=$( cd "$( dirname "$TASK_FILE" )" && pwd )
ROOTDIR="$MYDIR/../"
source "$ROOTDIR/env/util.env"
source "$ROOTDIR/env/prereq_bash.env"
source "$ROOTDIR/env/database_local.env"
MYDIR=$( cd "$( dirname "$TASK_FILE" )" && pwd )  # MYDIR gets clobbered

set -o xtrace

# Create pairs of id, expr_id, prompt, description
pgsql "COPY (SELECT id, expression_id, prompt, description FROM cc_numericexpressionresponse_train ORDER BY expression_id, id) TO STDOUT WITH CSV HEADER DELIMITER E'\t'" |\
  sed -r 's#≈#=#;s#×#*#g;s#</?[bu]>##g' |\
  python scripts/simplify_prompt.py >\
  data/generation_pairs.tsv

# Done
wait  # Very important: wait for the jobs to actually finish...
set -o errexit
msg "DONE loading data"
msg "DONE with `basename $0`"

