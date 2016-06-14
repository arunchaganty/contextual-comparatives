#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

"""

import csv
import sys

from collections import defaultdict

def get_references(fstream):
    reader = csv.reader(fstream, delimiter="\t")
    refs = defaultdict(list)

    header = next(reader)
    assert header == ["id", "expr_id", "prompt", "simple_prompt", "description"]

    for _, expr_id, _, _, description in reader:
        refs[expr_id].append(description)

    return refs


def do_command(args):
    reader = csv.reader(args.input, delimiter="\t")
    writer = csv.writer(args.output, delimiter="\t")

    refs = get_references(args.references)

    header = next(reader)
    assert header == ["id", "input", "output", "score"]

    for id, _, output, _ in reader:
        assert len(refs[id]) > 0, "Couldn't find the right references"
        writer.writerow([output,] + refs[id])

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser( description='' )
    parser.add_argument('--input', type=argparse.FileType('r'), default=sys.stdin, help="")
    parser.add_argument('--references', type=argparse.FileType('r'), help="Contains reference descriptions")
    parser.add_argument('--output', type=argparse.FileType('w'), default=sys.stdout, help="")
    parser.set_defaults(func=do_command)

    #subparsers = parser.add_subparsers()
    #command_parser = subparsers.add_parser('command', help='' )
    #command_parser.set_defaults(func=do_command)

    ARGS = parser.parse_args()
    ARGS.func(ARGS)
