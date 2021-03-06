#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

"""

from __future__ import division

import csv
import sys
from collections import Counter

def do_command(args):
    reader = csv.reader(args.input, delimiter="\t")
    writer = csv.writer(args.output, delimiter="\t")
    header = next(reader)
    assert header == "id	baseline_perspective	generation_perspective	baseline_votes	generation_votes	none_votes	n_votes	error_analysis".split(), "invalid header: " + header

    counter = Counter()
    for id, baseline_perspective, generation_perspective, baseline_votes, generation_votes, none_votes, n_votes, error_analysis in reader:
        #if generation_perspective == "": continue
        generation_wins = int(generation_votes) >= int(n_votes)/2
        if not generation_wins:
            counter[error_analysis] += 1

    writer.writerow(["Cause", "# Mentions"])
    for cause, count in counter.items():
        writer.writerow([cause, count])

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser( description='' )
    parser.add_argument('--input', type=argparse.FileType('r'), default=sys.stdin, help="")
    parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="")
    parser.set_defaults(func=do_command)

    #subparsers = parser.add_subparsers()
    #command_parser = subparsers.add_parser('command', help='' )
    #command_parser.set_defaults(func=do_command)

    ARGS = parser.parse_args()
    ARGS.func(ARGS)
