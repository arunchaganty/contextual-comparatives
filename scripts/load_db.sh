#!/bin/bash

set -o nounset
set -o errexit

# Load the prerequisite environment variables
TASK_FILE="${BASH_SOURCE[0]}"
if [[ "$TASK_FILE" != /* ]]; then TASK_FILE="`pwd`/$TASK_FILE"; fi
MYDIR=$( cd "$( dirname "$TASK_FILE" )" && pwd )
ROOTDIR="$MYDIR/../"
source "$ROOTDIR/env/util.env"
#source "$ROOTDIR/env/prereq_bash.env"
source "$ROOTDIR/env/database.env"
MYDIR=$( cd "$( dirname "$TASK_FILE" )" && pwd )  # MYDIR gets clobbered

set -o xtrace

msg "Loading backed up db"
rsync jamie.stanford.edu:dump/cc.sql data/cc.sql
dropdb cc;
createdb --owner cc cc;
psql cc < data/cc.sql;
msg "done!"

