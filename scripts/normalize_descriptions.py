#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
"""
Normalize task descriptions
"""

import csv
import sys
import re



def do_command(args):
    IS_ABOUT_PATTERN = re.compile(r'.*is about (.*)')
    writer = csv.writer(args.output, delimiter='\t')
    for row in csv.reader(args.input, delimiter='\t'):
        id, description = row
        # First, simplify any row that contains the pattern "X is about ...".
        if IS_ABOUT_PATTERN.match(description):
            description = IS_ABOUT_PATTERN.match(description).group(1)
        # check spellings!


        writer.writerow([id, description])


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
