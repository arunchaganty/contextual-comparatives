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
cat $filename | \
  pgsql "COPY javanlp_sentence(id, doc_id, sentence_index, gloss, words, lemmas, pos_tags, ner_tags, doc_char_begin, doc_char_end, dependencies) 
         FROM STDIN WITH DELIMITER E'\t';"

# Done
wait  # Very important: wait for the jobs to actually finish...
set -o errexit
msg "DONE loading data"
msg "DONE with `basename $0`"


