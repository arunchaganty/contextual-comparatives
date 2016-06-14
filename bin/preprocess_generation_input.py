#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

"""

import csv
import sys

def do_command(args):
    reader = csv.reader(args.input, delimiter="\t")
    writer = csv.writer(args.output, delimiter="\t")

    header = next(reader)
    assert header == ["id", "expr_id", "prompt", "simple_prompt", "description"]

    writer.writerow(["id", "expression"]) # Corresponds to expression id and expression formula.
    ids_seen = set([])
    for _, expr_id, prompt, simple_prompt, _ in reader:
        if expr_id not in ids_seen:
            _, expression = prompt.split(" = ")
            #_, expression = simple_prompt.split(" = ")
            writer.writerow([expr_id, expression]) # Corresponds to expression id and expression formula.

            ids_seen.add(expr_id)

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
