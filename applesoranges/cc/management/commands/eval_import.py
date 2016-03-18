#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-
from django.core.management.base import BaseCommand, CommandError
from django.db import transaction
from cc.models import *
from javanlp.pgutil import parse_psql_array

import sys, csv

class Command(BaseCommand):
    """Reads inputs from file."""

    def add_arguments(self, parser):
        import argparse
        parser.add_argument('--input', type=argparse.FileType('r'), default=sys.stdin, help="Path to read file from")
        parser.add_argument('--system', type=str, required=True, help="Path to read file from")

    def handle(self, *args, **options):
        if options['input'] is None:
            raise CommandError("Must set input")

        system = options['system']
        reader = csv.reader(options['input'], delimiter='\t')
        header = next(reader)
        assert header == ["mention_id","multiplier","arguments","value","unit","score"]

        with transaction.atomic():
            for i, (mention_id, multiplier, arguments, value, unit, score) in enumerate(reader):
                # Type cast input
                mention_id, multiplier, arguments, value, unit, score  = int(mention_id), float(multiplier), list(map(int, parse_psql_array(arguments))), float(value), unit, float(score)

                # First, add a new expression.
                expr = NumericExpression.objects.create(
                    multiplier = multiplier,
                    arguments = arguments,
                    value = value,
                    unit = unit)
                expr.save()

                # Then add a perspective.
                perspective = NumericPerspectiveTask.objects.create(
                    mention = NumericMention.objects.get(id=mention_id),
                    expression = expr,
                    perspective = expr.perspective(),
                    system = system,
                    score = score)
                perspective.save()
                print("Processed row %d", i)

