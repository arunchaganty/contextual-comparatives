#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""

"""

import re
import csv
import sys

EXPR_RE = re.compile(r'([^(]+) \([^)]+\)')

def parse_expression(expr):
    """
    Split expression into the multiplier and the string contents of the tuples.
    e.g. 1/20 * 16 thousand USD per car (cost of a used car)
    would return '1/20', ['cost of a used car']
    """
    parts = [part.strip() for part in expr.split('*')]
    multiplier = parts[0]
    tuples = [part[part.find('(')+1:part.find(')')] for part in parts[1:]]

    return multiplier, tuples

def test_parse_expression():
    expr = '1/20 * 6 million guns per year (number of guns produced in the us) * 100 milliseconds (time taken to blink)'
    m, ts = parse_expression(expr)
    assert m == '1/20', "incorrect multiplier"
    assert ts == ["number of guns produced in the us", "time taken to blink"], "incorrect tuples"

def generate(expr):
    """
    Each expression has the format 1/20 * 16 thousand USD per car (cost of a used car)

    Baseline perspective:
    Generate a natural language description of a expr.
    If multiplier > 1: prepend "multipler times the "
    If multiplier < 1: prepend "multipler of the "
    If multiplier == 1: prepend "the "
    """
    multiplier, tuples = parse_expression(expr)

    output = ""

    # say the multiplier
    if multiplier == "1/1" or multiplier == "1":
        pass
    elif "/" in multiplier:
        output += multiplier + " of "
    else:
        output += multiplier + " times "
    output += "the "

    # append descriptions of each expression to the output
    output += " for ".join(tuples)

    return output

def do_command(args):
    reader = csv.reader(args.input, delimiter="\t")
    writer = csv.writer(args.output, delimiter='\t')

    header = next(reader)
    assert header == ['id', 'expression'], "invalid header"

    writer.writerow(['id', 'input', 'output', 'score'])

    for id, expression in reader:
        perspective = generate(expression)
        writer.writerow([id, expression, perspective, 1.0])

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
