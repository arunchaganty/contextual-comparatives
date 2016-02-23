#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""

"""

import re
import csv
import sys

def simplify(prompt):
    """
    simplifies a prompt of the form
    1.00 liters = 4 * 22 gallons per person per year (coffee consumption) * 1 minutes (a minute) * 1 thousand people (people killed by gun violence in 2016)
    to
    4 * coffee consumption * a minute * people killed by gun violence in 2016
    """
    _, prompt = prompt.split('=',2)
    parts = prompt.split('*')
    parts = [re.sub(r'[^(]+ \(([^)]+)\)', r'\g<1>', part) for part in parts]
    return ' * '.join(parts)

def do_command(args):
    writer = csv.writer(args.output, delimiter='\t')
    reader = csv.reader(args.input, delimiter='\t')
    header = next(reader)

    writer.writerow(["id", "expr_id", "prompt", "simple_prompt", "description"])
    for row in reader:
        id, expr_id, prompt, description = row
        simple_prompt = simplify(prompt)
        writer.writerow([id, expr_id, prompt, simple_prompt, description])

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
