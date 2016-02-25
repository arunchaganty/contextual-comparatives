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
#source "$ROOTDIR/env/prereq_bash.env"
source "$ROOTDIR/env/database.env"
MYDIR=$( cd "$( dirname "$TASK_FILE" )" && pwd )  # MYDIR gets clobbered

#set -o xtrace

# filename to load 
nargs=$#
if [[ $nargs != 1 ]]; then
  echo "Loads a javanlp input" 
  echo "Usage: $0 <filename>"
  exit 1;
fi;

filename=$1;
pgsql "COPY (SELECT 
  m.id, m.value, m.unit, m.normalized_value, m.normalized_unit, m.sentence_id, m.doc_char_begin, m.doc_char_end, m.token_begin, m.token_end,
  s.id, s.doc_id, s.sentence_index, s.gloss, s.words, s.lemmas, s.pos_tags, s.ner_tags, s.doc_char_begin, s.doc_char_end, s.dependencies
  FROM cc_numericmention_train m, javanlp_sentence s
  WHERE m.sentence_id = s.id
  ) TO STDOUT CSV HEADER DELIMITER E'\t';" > \
  $filename;

# Done
wait  # Very important: wait for the jobs to actually finish...
set -o errexit
msg "DONE loading data"
msg "DONE with `basename $0`"


